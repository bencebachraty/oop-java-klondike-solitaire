package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;

    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK
                || (activePile.getPileType() == Pile.PileType.DISCARD && !card.equals(activePile.getTopCard()))
                || card.isFaceDown())
            return;
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();

        if (activePile.getPileType() == Pile.PileType.TABLEAU && !card.equals(activePile.getTopCard())){
            int cardIndex = activePile.getCards().indexOf(card);
            for (int i = cardIndex; i < activePile.numOfCards(); i++) {
                Card currentCard = activePile.getCards().get(i);
                draggedCards.add(currentCard);

                currentCard.getDropShadow().setRadius(20);
                currentCard.getDropShadow().setOffsetX(10);
                currentCard.getDropShadow().setOffsetY(10);

                currentCard.toFront();
                currentCard.setTranslateX(offsetX);
                currentCard.setTranslateY(offsetY);
            }
        } else {
            draggedCards.add(card);

            card.getDropShadow().setRadius(20);
            card.getDropShadow().setOffsetX(10);
            card.getDropShadow().setOffsetY(10);

            card.toFront();
            card.setTranslateX(offsetX);
            card.setTranslateY(offsetY);
        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile pile = getValidIntersectingPile(card, tableauPiles);
        if (pile == null) {
            pile = getValidIntersectingPile(card, foundationPiles);
        }
        if (pile != null) {
            handleValidMove(card, pile);
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
    };

    public boolean isGameWon() {
        //TODO // 6. Winning condition
        return false;
    }

    public Game() {
        deck = Card.createNewDeck();
        Collections.shuffle(deck);
        initPiles();
        dealCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        int sizeOfDiscardPile = discardPile.numOfCards();

        for (int i = 0; i < sizeOfDiscardPile; i ++) {
            Card card = discardPile.getTopCard();
            card.moveToPile(stockPile);
            card.flip();
        }

        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        if (destPile == null
                || (destPile.getPileType() == Pile.PileType.FOUNDATION && destPile.isEmpty() && card.getRank() != Card.Rank.ACE)
                || (destPile.getPileType() == Pile.PileType.TABLEAU && destPile.isEmpty() && card.getRank() != Card.Rank.KING)) {
            return false;
        }

        if (destPile.getPileType() == Pile.PileType.FOUNDATION
                && destPile.isEmpty()
                && card.getRank() == Card.Rank.ACE) {
                return true;
        } else if (destPile.getPileType() == Pile.PileType.FOUNDATION
                    && destPile.getTopCard().getSuit() == card.getSuit()
                    && destPile.getTopCard().getRankValue() == card.getRankValue() - 1) {
            return true;
        } else if (destPile.getPileType() == Pile.PileType.TABLEAU
                    && destPile.isEmpty()
                    && card.getRank() == Card.Rank.KING) {
            return true;
        } else if (destPile.getPileType() == Pile.PileType.TABLEAU
                    && Card.isOppositeColor(card, destPile.getTopCard())
                    && destPile.getTopCard().getRankValue() == card.getRankValue() + 1) {
            return true;
        }
        return false;
    }

    private Pile  getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        Pile fromPile = card.getContainingPile();
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        int indexOfCardToFlip = fromPile.getCards().size() - (draggedCards.size() + 1);
        if (indexOfCardToFlip >= 0 && fromPile.getCards().get(indexOfCardToFlip).isFaceDown()) {
            fromPile.getCards().get(indexOfCardToFlip).flip();
        }
        draggedCards.clear();
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }

        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < i + 1; j++) {
                Card cardToAdd = deck.get(i);
                tableauPiles.get(i).addCard(cardToAdd);
                addMouseEventHandlers(cardToAdd);
                getChildren().add(cardToAdd);
                deck.remove(cardToAdd);
            }
        }
        for (Pile tableauPile: tableauPiles) {
            tableauPile.getTopCard().flip();
        }

        Iterator<Card> deckIterator = deck.iterator();

        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
