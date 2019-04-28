package com.codecool.klondike;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.*;

public class Card extends ImageView {

    private Suit suit;
    private Rank rank;
    private boolean faceDown;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile;
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;

    public Card(Suit suit, Rank rank, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public int getRankValue(){
        return rank.value;
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public String getShortName() {
        return "S" + suit.value + "R" + rank.value;
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {
        this.getContainingPile().getCards().remove(this);
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    @Override
    public String toString() {
        return "The " + "Rank" + rank.value + " of " + "Suit" + suit.value;
    }
    public static boolean isOppositeColor(Card card1, Card card2) {
        if ((card1.getSuit() == Suit.HEARTS || card1.getSuit() == Suit.DIAMONDS) && (card2.getSuit() == Suit.SPADES || card2.getSuit() == Suit.CLUBS)) {
            return true;
        } else if ((card1.getSuit() == Suit.SPADES || card1.getSuit() == Suit.CLUBS) && (card2.getSuit() == Suit.HEARTS || card2.getSuit() == Suit.DIAMONDS)) {
            return true;
        }
        return false;
    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

    public static List<Card> createNewDeck() {
        List<Card> result = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                result.add(new Card(suit, rank, true));
            }
        }
        return result;
    }

    public static void loadCardImages() {
        cardBackImage = new Image("card_images/card_back.png");
        String suitName = "";
        for (Suit suit : Suit.values()) {
            switch (suit) {
                case HEARTS:
                    suitName = "hearts";
                    break;
                case DIAMONDS:
                    suitName = "diamonds";
                    break;
                case SPADES:
                    suitName = "spades";
                    break;
                case CLUBS:
                    suitName = "clubs";
                    break;
            }
            for (Rank rank : Rank.values()) {
                String cardName = suitName + rank.value;
                String cardId = "S" + suit.value + "R" + rank.value;
                String imageFileName = "card_images/" + cardName + ".png";
                cardFaceImages.put(cardId, new Image(imageFileName));
            }
        }
    }

    public enum Suit {
        HEARTS(1),
        DIAMONDS(2),
        SPADES(3),
        CLUBS(4);
        private int value;

        Suit(int value) {
            this.value = value;
        }
    }

    public enum Rank {
        ACE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        TEN(10),
        JUMBO(11),
        QUEEN(12),
        KING(13);
        private int value;

        Rank(int value) {
            this.value = value;
        }
    }
}
