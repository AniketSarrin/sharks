export interface Event {
  id: string;
  title: string;
  description: string;
  date: string;
  time: string;
  endTime: string;
  location: string;
  address: string;
  city: string;
  category: string;
  capacity: number;
  attendees: number;
  price: number;
  image: string;
  organizerId?: string;
  organizer: {
    name: string;
    avatar: string;
    bio: string;
  };
  tags: string[];
  isFeatured?: boolean;
  status: "upcoming" | "ongoing" | "past" | "draft" | "pending";
}

export const categories = ["Music", "Networking", "Dating"];

export const mockEvents: Event[] = [
  {
    id: "1",
    title: "Summer Music Festival 2026",
    description: "Join us for the biggest outdoor music festival of the summer featuring top artists from around the world. Three stages, food trucks, art installations, and unforgettable memories await you.",
    date: "2026-07-15",
    time: "14:00",
    endTime: "23:00",
    location: "Golden Gate Park",
    address: "501 Stanyan St",
    city: "San Francisco, CA",
    category: "Music",
    capacity: 5000,
    attendees: 3420,
    price: 0,
    image: "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=800&q=80",
    organizer: { name: "Bay Area Events Co.", avatar: "https://ui-avatars.com/api/?name=BA&background=e8553a&color=fff", bio: "Premier event organizers in the Bay Area since 2015." },
    tags: ["outdoor", "live music", "festival"],
    isFeatured: true,
    status: "upcoming",
  },
  {
    id: "2",
    title: "Tech Startup Pitch Night",
    description: "Watch 10 innovative startups pitch their ideas to a panel of VCs. Network with founders, investors, and tech enthusiasts. Light refreshments provided.",
    date: "2026-05-20",
    time: "18:00",
    endTime: "21:00",
    location: "Innovation Hub",
    address: "200 Market St",
    city: "San Jose, CA",
    category: "Networking",
    capacity: 200,
    attendees: 156,
    price: 15,
    image: "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&q=80",
    organizer: { name: "StartupSJ", avatar: "https://ui-avatars.com/api/?name=SJ&background=7c3aed&color=fff", bio: "Connecting the startup ecosystem in Silicon Valley." },
    tags: ["networking", "startups", "venture capital"],
    isFeatured: true,
    status: "upcoming",
  },
  {
    id: "3",
    title: "Farm-to-Table Dinner Experience",
    description: "A curated 5-course dinner featuring locally sourced ingredients from nearby farms. Meet the chefs, learn about sustainable dining, and enjoy wine pairings.",
    date: "2026-06-08",
    time: "19:00",
    endTime: "22:00",
    location: "The Garden Table",
    address: "45 Oak Lane",
    city: "Napa, CA",
    category: "Dating",
    capacity: 60,
    attendees: 52,
    price: 85,
    image: "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800&q=80",
    organizer: { name: "Napa Culinary Arts", avatar: "https://ui-avatars.com/api/?name=NC&background=059669&color=fff", bio: "Celebrating culinary excellence in wine country." },
    tags: ["dinner", "wine", "farm-to-table"],
    status: "upcoming",
  },
  {
    id: "4",
    title: "Community Yoga in the Park",
    description: "Free outdoor yoga session for all levels. Bring your mat and water bottle. We'll flow through a relaxing vinyasa sequence with views of the bay.",
    date: "2026-05-25",
    time: "08:00",
    endTime: "09:30",
    location: "Marina Green",
    address: "Marina Blvd",
    city: "San Francisco, CA",
    category: "Music",
    capacity: 100,
    attendees: 67,
    price: 0,
    image: "https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?w=800&q=80",
    organizer: { name: "Zen Flow Studio", avatar: "https://ui-avatars.com/api/?name=ZF&background=0891b2&color=fff", bio: "Bringing mindfulness to the community." },
    tags: ["yoga", "outdoor", "wellness"],
    status: "upcoming",
  },
  {
    id: "5",
    title: "AI & Machine Learning Conference",
    description: "Two-day conference featuring keynotes, workshops, and panels on the latest in AI, deep learning, and data science. Hands-on coding sessions included.",
    date: "2026-08-10",
    time: "09:00",
    endTime: "17:00",
    location: "Moscone Center",
    address: "747 Howard St",
    city: "San Francisco, CA",
    category: "Networking",
    capacity: 2000,
    attendees: 1230,
    price: 299,
    image: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=800&q=80",
    organizer: { name: "TechForward", avatar: "https://ui-avatars.com/api/?name=TF&background=4f46e5&color=fff", bio: "Leading technology conferences worldwide." },
    tags: ["AI", "conference", "machine learning"],
    isFeatured: true,
    status: "upcoming",
  },
  {
    id: "6",
    title: "Street Art Walking Tour",
    description: "Discover the vibrant street art scene in the Mission District. Learn about the artists, history, and stories behind the murals.",
    date: "2026-06-01",
    time: "10:00",
    endTime: "12:30",
    location: "Mission District",
    address: "24th & Mission St",
    city: "San Francisco, CA",
    category: "Dating",
    capacity: 25,
    attendees: 18,
    price: 20,
    image: "https://images.unsplash.com/photo-1499781350541-7783f6c6a0c8?w=800&q=80",
    organizer: { name: "Urban Art Walks", avatar: "https://ui-avatars.com/api/?name=UA&background=db2777&color=fff", bio: "Exploring urban creativity one mural at a time." },
    tags: ["art", "walking tour", "murals"],
    status: "upcoming",
  },
  {
    id: "7",
    title: "Charity 5K Run for Education",
    description: "Run or walk to support local education initiatives. All proceeds go to underfunded schools in the area. Medals for all finishers!",
    date: "2026-06-15",
    time: "07:00",
    endTime: "11:00",
    location: "Lakeside Park",
    address: "666 Bellevue Ave",
    city: "Oakland, CA",
    category: "Dating",
    capacity: 500,
    attendees: 312,
    price: 30,
    image: "https://images.unsplash.com/photo-1452626038306-9aae5e071dd3?w=800&q=80",
    organizer: { name: "Run for Change", avatar: "https://ui-avatars.com/api/?name=RC&background=ea580c&color=fff", bio: "Making a difference one mile at a time." },
    tags: ["charity", "running", "education"],
    status: "upcoming",
  },
  {
    id: "8",
    title: "Documentary Film Screening",
    description: "Exclusive screening of 'The Last Frontier', an award-winning documentary about climate change. Q&A with the director follows.",
    date: "2026-05-30",
    time: "19:30",
    endTime: "22:00",
    location: "Roxie Theater",
    address: "3117 16th St",
    city: "San Francisco, CA",
    category: "Networking",
    capacity: 150,
    attendees: 98,
    price: 12,
    image: "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&q=80",
    organizer: { name: "Indie Film Society", avatar: "https://ui-avatars.com/api/?name=IF&background=9333ea&color=fff", bio: "Championing independent cinema." },
    tags: ["film", "documentary", "screening"],
    status: "upcoming",
  },
];
