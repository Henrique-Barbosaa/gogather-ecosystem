export interface User {
  id: string;
  name: string;
  email: string;
  avatar?: string;
  bio?: string;
  tripsCount: number;
  pixKey?: string;
  pixType?: string;
}

export interface Friend {
  id: string;
  name: string;
  email: string;
  avatar?: string;
  status: 'online' | 'offline' | 'busy' | 'pending';
  requestDirection?: 'incoming' | 'outgoing';
  sharedTripsCount: number;
}

export interface Expense {
  id: string;
  groupId: string;
  title: string;
  amount: number;
  paidBy: string; // user name or id
  paidById: string;
  date: string;
  category: 'Hospedagem' | 'Alimentação' | 'Transporte' | 'Lazer' | 'Outros';
  participants: string[]; // list of user ids
  contributions?: { userId: string; name: string; amount: number }[];
}

export interface ChatMessage {
  id: string;
  groupId: string;
  senderId: string;
  senderName: string;
  senderAvatar?: string;
  content: string;
  timestamp: string;
  type?: string;
}

export interface RoadmapEvent {
  id: string;
  groupId: string;
  day: number | string; // Day 1, Day 2, etc.
  dateStr?: string;
  time: string;
  title: string;
  location: string;
  description?: string;
  category: 'Chegada' | 'Passeio' | 'Refeição' | 'Festa' | 'Partida' | 'Outros';
}

export type RoadmapItem = RoadmapEvent;

export interface Member {
  id: string;
  name: string;
  email: string;
  role: 'admin' | 'membro';
  avatar?: string;
}

export interface TravelGroup {
  id: string;
  inviteCode?: string;
  title: string;
  destination: string;
  startDate: string;
  endDate: string;
  coverUrl: string;
  description: string;
  isMuted?: boolean;
  members: Member[];
  expenses: Expense[];
  messages: ChatMessage[];
  roadmap: RoadmapEvent[];
}

export function mapExpenseFromApi(exp: any, fallbackGroupId: string): Expense {
  const contribs = exp.contributions || [];
  const paidByName = contribs.length > 1
    ? `${contribs.map((c: any) => c.displayName || c.username || c.name || 'Alguém').join(', ')} (${contribs.length} contribuidores)`
    : (contribs[0]?.displayName || contribs[0]?.username || contribs[0]?.name || exp.paidBy || 'Alguém');
  const paidById = String(contribs[0]?.userId || exp.paidById || '');

  return {
    id: String(exp.id),
    groupId: String(exp.groupId || fallbackGroupId),
    title: exp.description || exp.title || 'Despesa',
    amount: (exp.totalCents !== undefined ? exp.totalCents / 100 : exp.amount) || 0,
    paidBy: paidByName,
    paidById: paidById,
    date: exp.expenseDate || exp.date || new Date().toISOString().split('T')[0],
    category: (exp.category as any) || 'Outros',
    participants: exp.participants?.map((p: any) => String(p.userId || p.id || p)) || [],
    contributions: contribs.map((c: any) => ({
      userId: String(c.userId),
      name: c.displayName || c.username || c.name || 'Membro',
      amount: (c.amountInCents !== undefined ? c.amountInCents / 100 : c.amount) || 0
    }))
  };
}

// Initial empty/neutral state without any mocked data
export const initialUser: User = {
  id: '',
  name: 'Viajante',
  email: '',
  avatar: '',
  bio: '',
  tripsCount: 0,
  pixKey: '',
  pixType: 'CPF',
};

export const initialFriends: Friend[] = [];

export const initialGroups: TravelGroup[] = [];

// Helper to generate clean UI Avatars instead of random girl photos or mocked images
export function getAvatarUrl(name?: string, avatar?: string): string {
  if (avatar && avatar.trim() !== '' && !avatar.includes('unsplash.com')) {
    return avatar;
  }
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(name || 'User')}&background=random&color=fff&size=256&font-size=0.4`;
}

// Curated helper to return custom cover or fetch a beautiful destination image to replace template placeholders
export function getGroupCoverUrl(destination?: string, customCoverUrl?: string): string {
  const isTemplate = (url?: string) => {
    if (!url) return true;
    return (
      url.includes('photo-1507525428034-b723cf961d3e') ||
      url.includes('photo-1519046904884-53103b34b206') ||
      url.trim() === ''
    );
  };

  if (customCoverUrl && !isTemplate(customCoverUrl)) {
    return customCoverUrl;
  }

  const dest = (destination || '').toLowerCase().trim();

  const destMap: { [key: string]: string } = {
    'florianópolis': 'https://images.unsplash.com/photo-1512753360425-42d3479bc9c3?auto=format&fit=crop&w=1200&q=80',
    'floripa': 'https://images.unsplash.com/photo-1512753360425-42d3479bc9c3?auto=format&fit=crop&w=1200&q=80',
    'rio de janeiro': 'https://images.unsplash.com/photo-1483729558449-99ef09a8c325?auto=format&fit=crop&w=1200&q=80',
    'rio': 'https://images.unsplash.com/photo-1483729558449-99ef09a8c325?auto=format&fit=crop&w=1200&q=80',
    'são paulo': 'https://images.unsplash.com/photo-1543087903-1ac2ec7aa8c5?auto=format&fit=crop&w=1200&q=80',
    'sp': 'https://images.unsplash.com/photo-1543087903-1ac2ec7aa8c5?auto=format&fit=crop&w=1200&q=80',
    'paris': 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=1200&q=80',
    'londres': 'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?auto=format&fit=crop&w=1200&q=80',
    'london': 'https://images.unsplash.com/photo-1513635269975-59663e0ac1ad?auto=format&fit=crop&w=1200&q=80',
    'roma': 'https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=1200&q=80',
    'rome': 'https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=1200&q=80',
    'nova york': 'https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?auto=format&fit=crop&w=1200&q=80',
    'new york': 'https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?auto=format&fit=crop&w=1200&q=80',
    'ny': 'https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?auto=format&fit=crop&w=1200&q=80',
    'nyc': 'https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?auto=format&fit=crop&w=1200&q=80',
    'tóquio': 'https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=1200&q=80',
    'tokyo': 'https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=1200&q=80',
    'machu picchu': 'https://images.unsplash.com/photo-1509024644558-2f56ce76c490?auto=format&fit=crop&w=1200&q=80',
    'buenos aires': 'https://images.unsplash.com/photo-1589909202802-8f4aadce1849?auto=format&fit=crop&w=1200&q=80',
    'lisboa': 'https://images.unsplash.com/photo-1509840841025-9088ba78a826?auto=format&fit=crop&w=1200&q=80',
    'lisbon': 'https://images.unsplash.com/photo-1509840841025-9088ba78a826?auto=format&fit=crop&w=1200&q=80',
    'barcelona': 'https://images.unsplash.com/photo-1583899828474-7cf8c58be8a6?auto=format&fit=crop&w=1200&q=80',
    'cancun': 'https://images.unsplash.com/photo-1552074284-5e88ef1aef18?auto=format&fit=crop&w=1200&q=80',
    'salvador': 'https://images.unsplash.com/photo-1598463137979-e58f0cb18a4a?auto=format&fit=crop&w=1200&q=80',
    'nordeste': 'https://images.unsplash.com/photo-1590001155093-a3c66ab0c3ff?auto=format&fit=crop&w=1200&q=80',
    'praia': 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80',
    'beach': 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80',
    'neve': 'https://images.unsplash.com/photo-1482867996988-2faec3cbb4f9?auto=format&fit=crop&w=1200&q=80',
    'snow': 'https://images.unsplash.com/photo-1482867996988-2faec3cbb4f9?auto=format&fit=crop&w=1200&q=80',
    'montanha': 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1200&q=80',
    'mountain': 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1200&q=80'
  };

  for (const [key, url] of Object.entries(destMap)) {
    if (dest.includes(key)) {
      return url;
    }
  }

  const fallbacks = [
    'https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=1200&q=80',
    'https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?auto=format&fit=crop&w=1200&q=80',
    'https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=1200&q=80',
    'https://images.unsplash.com/photo-1527631746610-bca00a040d60?auto=format&fit=crop&w=1200&q=80',
    'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=1200&q=80',
  ];

  let hash = 0;
  for (let i = 0; i < dest.length; i++) {
    hash = dest.charCodeAt(i) + ((hash << 5) - hash);
  }
  const index = Math.abs(hash) % fallbacks.length;
  return fallbacks[index];
}
