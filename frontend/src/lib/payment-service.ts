/**
 * Mock Payment Service
 * ---------------------
 * A client-side payment stub that simulates a real payment gateway.
 * It validates card details (Luhn check, expiry, CVV) and approves or
 * declines the transaction based on a set of magic test card numbers.
 *
 * This is NOT a real payment processor. Do not use in production.
 */

export interface PaymentRequest {
  /** Raw card number (digits, spaces allowed) */
  cardNumber: string;
  /** MM/YY or MM/YYYY */
  expiry: string;
  /** 3-4 digit CVV */
  cvv: string;
  /** Cardholder name */
  cardholderName: string;
  /** Amount in smallest currency unit (e.g. cents) — must be > 0 */
  amount: number;
  /** ISO 4217 currency code, e.g. "USD" */
  currency: string;
  /** Optional human-readable description (e.g. event title) */
  description?: string;
  /** Optional client-supplied reference (e.g. event id, order id) */
  reference?: string;
}

export type PaymentStatus = "succeeded" | "failed";

export type PaymentDeclineCode =
  | "invalid_card_number"
  | "invalid_expiry"
  | "expired_card"
  | "invalid_cvv"
  | "invalid_amount"
  | "missing_cardholder"
  | "insufficient_funds"
  | "card_declined"
  | "processing_error";

export interface PaymentResponse {
  status: PaymentStatus;
  /** Unique mock transaction id */
  transactionId: string;
  amount: number;
  currency: string;
  /** Last 4 digits of the card used */
  cardLast4: string;
  /** Card brand inferred from BIN */
  cardBrand: string;
  /** ISO timestamp when the payment was processed */
  processedAt: string;
  reference?: string;
  /** Decline code if status === "failed" */
  declineCode?: PaymentDeclineCode;
  /** Human-readable message */
  message: string;
}

/**
 * Magic test card numbers — mirror Stripe's test card conventions so
 * developers have predictable behaviour for demos and tests.
 */
const TEST_CARDS: Record<string, { result: PaymentStatus; declineCode?: PaymentDeclineCode; message: string }> = {
  "4242424242424242": { result: "succeeded", message: "Payment approved" },
  "4000000000000002": { result: "failed", declineCode: "card_declined", message: "Your card was declined" },
  "4000000000009995": { result: "failed", declineCode: "insufficient_funds", message: "Insufficient funds" },
  "4000000000000069": { result: "failed", declineCode: "expired_card", message: "Card has expired" },
  "4000000000000127": { result: "failed", declineCode: "invalid_cvv", message: "Incorrect CVV" },
  "4000000000000119": { result: "failed", declineCode: "processing_error", message: "Processing error, please try again" },
};

/** Luhn algorithm — validates card number checksum. */
function luhnCheck(cardNumber: string): boolean {
  const digits = cardNumber.replace(/\D/g, "");
  if (digits.length < 12 || digits.length > 19) return false;

  let sum = 0;
  let shouldDouble = false;
  for (let i = digits.length - 1; i >= 0; i--) {
    let d = parseInt(digits[i], 10);
    if (shouldDouble) {
      d *= 2;
      if (d > 9) d -= 9;
    }
    sum += d;
    shouldDouble = !shouldDouble;
  }
  return sum % 10 === 0;
}

/** Infer card brand from BIN (issuer identification number). */
function detectBrand(cardNumber: string): string {
  const n = cardNumber.replace(/\D/g, "");
  if (/^4/.test(n)) return "Visa";
  if (/^(5[1-5]|2[2-7])/.test(n)) return "Mastercard";
  if (/^3[47]/.test(n)) return "Amex";
  if (/^6(?:011|5)/.test(n)) return "Discover";
  return "Unknown";
}

/** Parse "MM/YY" or "MM/YYYY" expiry into month/year numbers. */
function parseExpiry(expiry: string): { month: number; year: number } | null {
  const match = expiry.trim().match(/^(\d{1,2})\s*\/\s*(\d{2}|\d{4})$/);
  if (!match) return null;
  const month = parseInt(match[1], 10);
  let year = parseInt(match[2], 10);
  if (year < 100) year += 2000;
  if (month < 1 || month > 12) return null;
  return { month, year };
}

function isExpired(month: number, year: number): boolean {
  const now = new Date();
  // expiry is end of the given month
  const expiryDate = new Date(year, month, 0, 23, 59, 59);
  return expiryDate.getTime() < now.getTime();
}

function generateTransactionId(): string {
  const rand = Math.random().toString(36).slice(2, 12);
  return `mock_txn_${Date.now().toString(36)}_${rand}`;
}

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * Process a mock payment.
 *
 * Returns a PaymentResponse describing the outcome. Network/processing
 * latency is simulated with a short delay so UIs can show a loading state.
 */
export async function processPayment(req: PaymentRequest): Promise<PaymentResponse> {
  // Simulate network + processing latency (600-1400ms)
  await delay(600 + Math.random() * 800);

  const cardNumber = (req.cardNumber || "").replace(/\s+/g, "");
  const last4 = cardNumber.slice(-4);
  const brand = detectBrand(cardNumber);
  const baseResponse = {
    transactionId: generateTransactionId(),
    amount: req.amount,
    currency: (req.currency || "USD").toUpperCase(),
    cardLast4: last4,
    cardBrand: brand,
    processedAt: new Date().toISOString(),
    reference: req.reference,
  };

  // --- Validation ---
  if (!req.cardholderName || !req.cardholderName.trim()) {
    return { ...baseResponse, status: "failed", declineCode: "missing_cardholder", message: "Cardholder name is required" };
  }
  if (!req.amount || req.amount <= 0) {
    return { ...baseResponse, status: "failed", declineCode: "invalid_amount", message: "Amount must be greater than zero" };
  }
  if (!luhnCheck(cardNumber)) {
    return { ...baseResponse, status: "failed", declineCode: "invalid_card_number", message: "Invalid card number" };
  }
  const expiry = parseExpiry(req.expiry || "");
  if (!expiry) {
    return { ...baseResponse, status: "failed", declineCode: "invalid_expiry", message: "Invalid expiry date" };
  }
  if (isExpired(expiry.month, expiry.year)) {
    return { ...baseResponse, status: "failed", declineCode: "expired_card", message: "Card has expired" };
  }
  if (!/^\d{3,4}$/.test((req.cvv || "").trim())) {
    return { ...baseResponse, status: "failed", declineCode: "invalid_cvv", message: "Invalid CVV" };
  }

  // --- Magic test cards take precedence ---
  const magic = TEST_CARDS[cardNumber];
  if (magic) {
    return {
      ...baseResponse,
      status: magic.result,
      declineCode: magic.declineCode,
      message: magic.message,
    };
  }

  // --- Default behaviour: ~92% approval for any other valid card ---
  const approved = Math.random() < 0.92;
  if (approved) {
    return { ...baseResponse, status: "succeeded", message: "Payment approved" };
  }
  return {
    ...baseResponse,
    status: "failed",
    declineCode: "card_declined",
    message: "Your card was declined",
  };
}

/**
 * Verify the status of a previously created transaction. In this mock
 * implementation any well-formed transaction id is treated as succeeded —
 * useful for callback/redirect flows where the UI just needs confirmation.
 */
export async function verifyPayment(transactionId: string): Promise<{ valid: boolean; status: PaymentStatus; transactionId: string }> {
  await delay(200);
  const valid = /^mock_txn_[a-z0-9]+_[a-z0-9]+$/i.test(transactionId);
  return {
    valid,
    status: valid ? "succeeded" : "failed",
    transactionId,
  };
}

/** Convenient export of test cards so UIs / docs can list them. */
export const MOCK_TEST_CARDS = {
  success: "4242 4242 4242 4242",
  declined: "4000 0000 0000 0002",
  insufficientFunds: "4000 0000 0000 9995",
  expired: "4000 0000 0000 0069",
  invalidCvv: "4000 0000 0000 0127",
  processingError: "4000 0000 0000 0119",
} as const;
