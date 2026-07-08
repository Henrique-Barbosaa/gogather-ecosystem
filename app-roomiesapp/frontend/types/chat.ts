export interface PollOptionResponse {
  id: number;
  text: string;
  placeId: string | null;
  votes: number;
  voterIds: number[];
}

export interface PollResponse {
  id: number;
  options: PollOptionResponse[];
}

/**
 * Mensagem de chat. Alinhada ao ChatMessageResponse do backend roomiesapp:
 * id, senderId, senderName, content, type (USER | SYSTEM | POLL), createdAt, poll.
 */
export interface ChatMessage {
  id?: string;
  senderId?: string | null;
  senderName: string;
  content: string;
  type: "USER" | "SYSTEM" | "POLL";
  createdAt: string;
  poll?: PollResponse | null;
}

export interface TypingEvent {
  senderName: string;
  isTyping: boolean;
}

export interface GroupMemberDTO {
  id: string;
  username: string;
  name?: string;
  role?: string;
  email?: string;
}

/**
 * Detalhes do grupo usados no cabeçalho do chat.
 * Alinhado ao GroupResponse do roomiesapp (sem eventDate/eventStops).
 * `members` é opcional pois o GroupResponse atual não retorna a lista.
 */
export interface GroupDetails {
  id: number;
  externalId: string;
  name: string;
  description: string;
  inviteCode: string;
  address?: string | null;
  monthlyRentCents?: number | null;
  maxOccupants?: number;
  createdAt: string;
  members?: GroupMemberDTO[];
}

export interface Sort {
  empty: boolean;
  sorted: boolean;
  unsorted: boolean;
}

export interface Pageable {
  pageNumber: number;
  pageSize: number;
  sort: Sort;
  offset: number;
  paged: boolean;
  unpaged: boolean;
}

export interface PaginatedChatHistory {
  content: ChatMessage[];
  pageable: Pageable;
  last: boolean;
  totalPages: number;
  totalElements: number;
  first: boolean;
  size: number;
  number: number;
  sort: Sort;
  numberOfElements: number;
  empty: boolean;
}
