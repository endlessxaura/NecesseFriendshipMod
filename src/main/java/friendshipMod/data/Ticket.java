package friendshipMod.data;

public class Ticket {
    public enum Kind {
        Human,
        Animal,
        Item
    }

    public String ticketId;
    public Kind kind;

    public Ticket(String ticketId, Kind kind) {
        this.ticketId = ticketId;
        this.kind = kind;
    }
}
