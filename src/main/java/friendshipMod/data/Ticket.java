package friendshipMod.data;

public class Ticket {
    public enum Kind {
        Human,
        Animal,
        Item
    }

    public int ticketId;
    public Kind kind;

    public Ticket(int ticketId, Kind kind) {
        this.ticketId = ticketId;
        this.kind = kind;
    }
}
