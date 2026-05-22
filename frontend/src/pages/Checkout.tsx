import { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { ArrowLeft, CreditCard, Lock, CheckCircle2, XCircle, Loader2, Copy } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import Navbar from "@/components/NavBar";
import Footer from "@/components/Footer";
import { eventsApi } from "@/lib/events-api";
import { ticketingApi } from "@/lib/ticketing-api";
import type { Event } from "@/lib/mock-data";
import type { TicketReceiptResponse } from "@/lib/api-types";
import {
  processPayment,
  MOCK_TEST_CARDS,
  type PaymentResponse,
} from "@/lib/payment-service";
import { toast } from "sonner";

const formatCardNumber = (value: string) =>
  value
    .replace(/\D/g, "")
    .slice(0, 19)
    .replace(/(\d{4})(?=\d)/g, "$1 ");

const formatExpiry = (value: string) => {
  const digits = value.replace(/\D/g, "").slice(0, 4);
  if (digits.length < 3) return digits;
  return `${digits.slice(0, 2)}/${digits.slice(2)}`;
};

const Checkout = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [event, setEvent] = useState<Event | null>(null);
  const [loadingEvent, setLoadingEvent] = useState(true);

  const [quantity, setQuantity] = useState(1);
  const [cardNumber, setCardNumber] = useState("");
  const [expiry, setExpiry] = useState("");
  const [cvv, setCvv] = useState("");
  const [cardholderName, setCardholderName] = useState("");
  const [email, setEmail] = useState("");
  const [processing, setProcessing] = useState(false);
  const [paymentResult, setPaymentResult] = useState<PaymentResponse | null>(null);
  const [ticketReceipt, setTicketReceipt] = useState<TicketReceiptResponse | null>(null);

  useEffect(() => {
    if (!id) return;
    eventsApi.getById(id).then((res) => {
      if (res.success) setEvent(res.data);
      setLoadingEvent(false);
    });
  }, [id]);

  if (loadingEvent) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <div className="container mx-auto py-20 text-center">
          <p className="text-lg text-muted-foreground">Loading event...</p>
        </div>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <div className="container mx-auto py-20 text-center">
          <p className="text-lg text-muted-foreground">Event not found</p>
          <Link to="/">
            <Button variant="outline" className="mt-4">Back to events</Button>
          </Link>
        </div>
      </div>
    );
  }

  const unitPrice = ticketReceipt ? ticketReceipt.unitPrice : event.price;
  const subtotal = unitPrice * quantity;
  const fees = subtotal > 0 ? Math.round(subtotal * 0.05 * 100) / 100 : 0;
  const total = subtotal + fees;
  const totalCents = Math.round(total * 100);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (processing) return;

    setProcessing(true);
    setPaymentResult(null);

    try {
      // Run mock payment validation first so the user knows their card details are OK
      const payResponse = await processPayment({
        cardNumber,
        expiry,
        cvv,
        cardholderName,
        amount: totalCents,
        currency: "USD",
        description: event.title,
        reference: event.id,
      });

      setPaymentResult(payResponse);

      if (payResponse.status === "succeeded") {
        // Card was accepted — issue the ticket via the ticketing service
        const ticketRes = await ticketingApi.purchase(Number(event.id), quantity);
        if (ticketRes.success) {
          setTicketReceipt(ticketRes.data);
          toast.success("Payment successful!", {
            description: `Ticket code: ${ticketRes.data.ticketCode}`,
          });
        } else {
          toast.error("Payment succeeded but ticket issue failed", {
            description: ticketRes.error,
          });
        }
      } else {
        toast.error("Payment failed", { description: payResponse.message });
      }
    } catch (err) {
      toast.error("Unexpected error", {
        description: err instanceof Error ? err.message : "Please try again",
      });
    } finally {
      setProcessing(false);
    }
  };

  const copyTestCard = (num: string) => {
    const clean = num.replace(/\s/g, "");
    setCardNumber(formatCardNumber(clean));
    toast.success("Test card filled in");
  };

  // SUCCESS state — payment succeeded and ticket was issued
  if (paymentResult?.status === "succeeded" && ticketReceipt) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <main className="container mx-auto px-4 py-16">
          <div className="mx-auto max-w-xl rounded-xl border border-border bg-card p-8 text-center card-shadow">
            <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-success/10">
              <CheckCircle2 className="h-10 w-10 text-success" />
            </div>
            <h1 className="mb-2 text-2xl font-extrabold text-card-foreground">Booking Confirmed</h1>
            <p className="mb-6 text-muted-foreground">
              You're registered for <span className="font-medium text-foreground">{event.title}</span>.
            </p>

            <div className="mb-6 space-y-2 rounded-lg bg-muted/40 p-4 text-left text-sm">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Ticket code</span>
                <span className="font-mono text-xs text-card-foreground">{ticketReceipt.ticketCode}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Card</span>
                <span className="text-card-foreground">{paymentResult.cardBrand} •••• {paymentResult.cardLast4}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Total paid</span>
                <span className="font-semibold text-card-foreground">
                  ${ticketReceipt.totalPrice.toFixed(2)} USD
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Quantity</span>
                <span className="text-card-foreground">{ticketReceipt.quantity} ticket{ticketReceipt.quantity > 1 ? "s" : ""}</span>
              </div>
            </div>

            <div className="flex flex-col gap-2 sm:flex-row">
              <Button className="flex-1" onClick={() => navigate("/my-tickets")}>View my tickets</Button>
              <Button variant="outline" className="flex-1" onClick={() => navigate("/")}>Back to home</Button>
            </div>
          </div>
        </main>
        <Footer />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <Navbar />

      <main className="container mx-auto px-4 py-8">
        <Link
          to={`/event/${event.id}`}
          className="mb-6 inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" /> Back to event
        </Link>

        <div className="grid gap-8 lg:grid-cols-3">
          {/* Payment form */}
          <div className="lg:col-span-2">
            <div className="rounded-xl border border-border bg-card p-6 md:p-8 card-shadow">
              <div className="mb-6 flex items-center gap-2">
                <CreditCard className="h-5 w-5 text-primary" />
                <h1 className="text-2xl font-extrabold text-card-foreground">Payment Details</h1>
              </div>

              {paymentResult?.status === "failed" && (
                <div className="mb-6 flex items-start gap-3 rounded-lg border border-destructive/30 bg-destructive/10 p-4">
                  <XCircle className="mt-0.5 h-5 w-5 shrink-0 text-destructive" />
                  <div className="text-sm">
                    <p className="font-semibold text-destructive">{paymentResult.message}</p>
                    {paymentResult.declineCode && (
                      <p className="mt-1 text-muted-foreground">
                        Code: <span className="font-mono">{paymentResult.declineCode}</span>
                      </p>
                    )}
                  </div>
                </div>
              )}

              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <Label htmlFor="email">Email for receipt</Label>
                  <Input
                    id="email"
                    type="email"
                    required
                    placeholder="you@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="mt-1.5"
                  />
                </div>

                <div>
                  <Label htmlFor="cardholder">Cardholder name</Label>
                  <Input
                    id="cardholder"
                    required
                    placeholder="Name on card"
                    value={cardholderName}
                    onChange={(e) => setCardholderName(e.target.value)}
                    className="mt-1.5"
                  />
                </div>

                <div>
                  <Label htmlFor="card">Card number</Label>
                  <div className="relative mt-1.5">
                    <Input
                      id="card"
                      required
                      inputMode="numeric"
                      autoComplete="cc-number"
                      placeholder="4242 4242 4242 4242"
                      value={cardNumber}
                      onChange={(e) => setCardNumber(formatCardNumber(e.target.value))}
                      className="pr-10"
                    />
                    <CreditCard className="absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="expiry">Expiry (MM/YY)</Label>
                    <Input
                      id="expiry"
                      required
                      inputMode="numeric"
                      autoComplete="cc-exp"
                      placeholder="12/28"
                      value={expiry}
                      onChange={(e) => setExpiry(formatExpiry(e.target.value))}
                      className="mt-1.5"
                    />
                  </div>
                  <div>
                    <Label htmlFor="cvv">CVV</Label>
                    <Input
                      id="cvv"
                      required
                      inputMode="numeric"
                      autoComplete="cc-csc"
                      placeholder="123"
                      maxLength={4}
                      value={cvv}
                      onChange={(e) => setCvv(e.target.value.replace(/\D/g, "").slice(0, 4))}
                      className="mt-1.5"
                    />
                  </div>
                </div>

                <Button type="submit" size="lg" className="w-full" disabled={processing}>
                  {processing ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Processing…
                    </>
                  ) : (
                    <>
                      <Lock className="mr-2 h-4 w-4" />
                      {subtotal > 0 ? `Pay $${total.toFixed(2)}` : "Confirm Free Registration"}
                    </>
                  )}
                </Button>

                <p className="flex items-center justify-center gap-1 text-xs text-muted-foreground">
                  <Lock className="h-3 w-3" /> This is a mock payment page. No real charges are made.
                </p>
              </form>

              {/* Test cards helper */}
              <div className="mt-8 rounded-lg border border-dashed border-border p-4">
                <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Test cards (click to fill)
                </p>
                <div className="space-y-1.5">
                  {[
                    { label: "Success", value: MOCK_TEST_CARDS.success },
                    { label: "Declined", value: MOCK_TEST_CARDS.declined },
                    { label: "Insufficient funds", value: MOCK_TEST_CARDS.insufficientFunds },
                    { label: "Expired", value: MOCK_TEST_CARDS.expired },
                    { label: "Invalid CVV", value: MOCK_TEST_CARDS.invalidCvv },
                  ].map((card) => (
                    <button
                      key={card.value}
                      type="button"
                      onClick={() => copyTestCard(card.value)}
                      className="flex w-full items-center justify-between rounded-md px-2 py-1.5 text-left text-sm hover:bg-muted"
                    >
                      <span className="font-mono text-xs text-card-foreground">{card.value}</span>
                      <span className="flex items-center gap-1 text-xs text-muted-foreground">
                        {card.label} <Copy className="h-3 w-3" />
                      </span>
                    </button>
                  ))}
                </div>
                <p className="mt-3 text-xs text-muted-foreground">
                  Use any future expiry and any 3-digit CVV.
                </p>
              </div>
            </div>
          </div>

          {/* Order summary */}
          <div className="lg:col-span-1">
            <div className="sticky top-20 rounded-xl border border-border bg-card p-6 card-shadow">
              <h2 className="mb-4 text-lg font-bold text-card-foreground">Order Summary</h2>

              <div className="mb-4 flex gap-3">
                <img
                  src={event.image}
                  alt={event.title}
                  className="h-16 w-16 rounded-lg object-cover"
                />
                <div className="flex-1">
                  <p className="font-semibold text-card-foreground line-clamp-2">{event.title}</p>
                  <p className="text-xs text-muted-foreground">{event.city}</p>
                  <Badge className="mt-1 bg-primary/10 text-primary border-0 text-xs">
                    {event.category}
                  </Badge>
                </div>
              </div>

              <div className="mb-4">
                <Label htmlFor="qty" className="text-xs text-muted-foreground">Quantity</Label>
                <div className="mt-1.5 flex items-center gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                  >
                    −
                  </Button>
                  <span className="w-10 text-center font-semibold text-card-foreground">{quantity}</span>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => setQuantity((q) => Math.min(10, q + 1))}
                  >
                    +
                  </Button>
                </div>
              </div>

              <Separator className="my-4" />

              <div className="space-y-2 text-sm">
                <div className="flex justify-between text-muted-foreground">
                  <span>Subtotal</span>
                  <span>${subtotal.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-muted-foreground">
                  <span>Service fee</span>
                  <span>${fees.toFixed(2)}</span>
                </div>
              </div>

              <Separator className="my-4" />

              <div className="flex justify-between text-base font-bold text-card-foreground">
                <span>Total</span>
                <span>${total.toFixed(2)} USD</span>
              </div>

              {event.price === 0 && (
                <p className="mt-3 text-xs text-muted-foreground">
                  This event is free, but the mock checkout still demonstrates the payment flow with a small service fee.
                </p>
              )}
            </div>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default Checkout;
