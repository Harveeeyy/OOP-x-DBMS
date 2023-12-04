package src;

public abstract class LibraryItem {
    protected String title;
    protected String author;
    protected String itemType;

    public LibraryItem(String title, String author, String itemType) {
        this.title = title;
        this.author = author;
        this.itemType = itemType;
    }

    public abstract void displayItem();
}