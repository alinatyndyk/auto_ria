export interface IMessage {
    id: number,
    content: string,
    senderId: number,
    receiverId: number,
    chatId: number,
    isEdited: boolean | null,
    updatedAt: number[],
    createdAt: number[]
}