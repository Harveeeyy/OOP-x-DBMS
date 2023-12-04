package src;

public class Book extends LibraryItem {
    private String genre;
    private int quantity;
    private boolean available = true;

    public Book(String title, String author, String genre) {
        super(title, author, "Book");
        this.genre = genre;
    }

    public Book(String title, String author) {
        super(title, author, "Book");
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public void displayItem() {
        System.out.println("Book Title: " + title);
        System.out.println("Book Author: " + author);
        System.out.println("Book Genre: " + genre);
        System.out.println("Quantity: " + quantity);
        System.out.println("Available: " + available);
    }
}
