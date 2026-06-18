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

export interface ChatMessage {
  content: string;
  senderName: string;
  type: "USER" | "AI";
  createdAt: string;
  poll?: PollResponse;
}

export interface TypingEvent {
  senderName: string;
  isTyping: boolean;
}

export interface EventStopDTO {
  name: string;
  latitude: number;
  longitude: number;
  category: string;
  stopOrder: number;
  city: string;
  state: string;
  placeId?: string;
}

export interface GroupDetails {
  externalId: string;
  name: string;
  description: string;
  inviteCode: string;
  createdAt: string;
  eventDate: string;
  members: GroupMemberDTO[];
  eventStops: EventStopDTO[];
}

export interface GroupMemberDTO {
  externalId: string;
  username: string;
  displayName: string;
  role: string;
  email: string;
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
