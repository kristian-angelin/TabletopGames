package games.terraformingmars.gui;

import core.components.Deck;
import games.terraformingmars.TMGameState;
import games.terraformingmars.TMTypes;
import games.terraformingmars.actions.*;
import games.terraformingmars.components.TMCard;
import games.terraformingmars.rules.effects.Effect;
import games.terraformingmars.rules.effects.PayForActionEffect;
import games.terraformingmars.rules.effects.PlaceTileEffect;
import games.terraformingmars.rules.effects.PlayCardEffect;
import games.terraformingmars.rules.requirements.CounterRequirement;
import games.terraformingmars.rules.requirements.Requirement;
import games.terraformingmars.rules.requirements.TagsPlayedRequirement;
import utilities.ImageIO;
import utilities.Vector2D;
import utilities.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import static core.AbstractGUI.defaultItemSize;
import static games.terraformingmars.gui.Utils.*;

public class TMDeckDisplay extends JComponent {

    Deck<TMCard> deck;
    TMGameState gs;

    HashMap<Rectangle, String> rects;  // Used for highlights + action trimming
    ArrayList<Rectangle> highlight;

    Image background;
    Image production;
    Image actionArrow;
    Image reqMin, reqMax;

    Image pointBg;
    Image projCardBg;
    int width, height;

    Color anyPlayerColor = new Color(234, 38, 38, 168);

    static int spacing = 10;
    static int cardHeight = 200;
    static int cardWidth;

    public TMDeckDisplay(TMGUI gui, TMGameState gs, Deck<TMCard> deck) {
        this.gs = gs;
        this.deck = deck;

        rects = new HashMap<>();
        highlight = new ArrayList<>();

        pointBg = ImageIO.GetInstance().getImage("data/terraformingmars/images/cards/card-point-bg.png");
        projCardBg = ImageIO.GetInstance().getImage("data/terraformingmars/images/cards/proj-card-bg.png");
        production = ImageIO.GetInstance().getImage("data/terraformingmars/images/misc/production.png");
        actionArrow = ImageIO.GetInstance().getImage("data/terraformingmars/images/misc/arrow.png");
        reqMin = ImageIO.GetInstance().getImage("data/terraformingmars/images/requisites/min_big.png");
        reqMax = ImageIO.GetInstance().getImage("data/terraformingmars/images/requisites/max_big.png");

        Vector2D dim = scaleLargestDimImg(projCardBg, cardHeight);
        cardWidth = dim.getX();
        if (deck != null) {
            width = deck.getSize() * cardWidth;
        } else {
            width = cardWidth;
        }
        height = cardHeight;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Left click, highlight cell
                    for (Rectangle r: rects.keySet()) {
                        if (r != null && r.contains(e.getPoint())) {
                            highlight.clear();
                            highlight.add(r);
                            break;
                        }
                    }
                    gui.updateButtons = true;
                } else {
                    // Remove highlight
                    highlight.clear();
                }
                gui.updateButtons = true;
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;

        g.setFont(TMGUI.defaultFont);

        if (deck != null) {
            // Draw player hand
            for (int i = 0; i < deck.getSize(); i++) {
//            if (playerHand.isComponentVisible(i, gs.getCurrentPlayer())) {
                if (deck.get(i) != null) {
                    int cardX = i * cardWidth;
                    int cardY = 0;
                    drawCard(g, deck.get(i), cardX, cardY, cardWidth, cardHeight);
                    rects.put(new Rectangle(cardX, cardY, cardWidth, cardHeight), ""+i);
                }
//            }
            }
        }

        if (highlight.size() > 0) {
            g.setColor(Color.green);
            Stroke s = g.getStroke();
            g.setStroke(new BasicStroke(3));

            Rectangle r = highlight.get(0);
            g.drawRect(r.x, r.y, r.width, r.height);
            g.setStroke(s);
        }
    }

    private void drawCard(Graphics2D g, TMCard card, int x, int y, int width, int height) {
        Rectangle aboveRibbon = new Rectangle(x + width/5, y, width - width/5 - spacing/2, height/8);
        if (card.cardType == TMTypes.CardType.Corporation) {
            Image img = ImageIO.GetInstance().getImage(TMTypes.CardType.Corporation.getImagePath());
            drawImage(g, img, x, y, height);
            // Draw name
            Font f = g.getFont();
            g.setFont(new Font("Arial", Font.BOLD, 14));
            Rectangle titleRect =  new Rectangle(x + 2, y + height/8 - 2, width - 4, height/8);
            drawStringCentered(g, card.getComponentName(), titleRect, Color.black, 14);
            g.setFont(f);
            // Draw tags
            int tagSize = defaultItemSize/3;
            int tagsWidth = card.tags.length * tagSize;
            int startX = (int)(aboveRibbon.getX() + aboveRibbon.getWidth() - tagsWidth);
            int tagY = (int)(aboveRibbon.getY() + aboveRibbon.getHeight()/2 - tagSize/2);
            for (int i = 0; i < card.tags.length; i++) {
                TMTypes.Tag tag = card.tags[i];
                Image img2 = ImageIO.GetInstance().getImage(tag.getImagePath());
                drawImage(g, img2, startX + i*tagSize, tagY, tagSize, tagSize);
            }
            // Draw starting resources
            int size = defaultItemSize/3;
            int yRes = titleRect.y + titleRect.height;
            for (TMAction aa: card.immediateEffects) {
                if (aa instanceof PlaceholderModifyCounter) {
                    int xRes = x + width/2;
                    drawPlaceHolderCounterActionMiddle(g, (PlaceholderModifyCounter) aa, xRes, yRes, size);
                    yRes += size + spacing / 5;
                }
            }
            // Draw actions
            int yA = yRes + spacing;
            for (TMAction a: card.actions) {
                int xA = x + width/2 - defaultItemSize/4 - size;
                yA += size + spacing / 5;
                drawAction(g, a, xA, yA, size);
            }
            // Draw discounts
            int yD = yA + spacing;
            for (Requirement r: card.discountEffects.keySet()) {
                if (r instanceof TagsPlayedRequirement) {
                    int xD = x + width/2 - size*6/2;
                    Image from = ImageIO.GetInstance().getImage(((TagsPlayedRequirement)r).tags[0].getImagePath());
                    Image to = ImageIO.GetInstance().getImage(TMTypes.Resource.MegaCredit.getImagePath());
                    int amount = card.discountEffects.get(r);
                    drawImage(g, from, xD, yD, size, size);
                    drawShadowStringCentered(g, ":", new Rectangle(xD + size, yD, size*2, size));
                    drawShadowStringCentered(g, "-" + amount, new Rectangle(xD + size*3, yD, size*2, size));
                    drawImage(g, to, xD + size * 5, yD, size, size);
                } else if (r instanceof CounterRequirement) {
                    int xD = x + width/2 - size*4/2;
                    TMTypes.GlobalParameter gp = Utils.searchEnum(TMTypes.GlobalParameter.class, ((CounterRequirement)r).counterCode);
                    String imgStr;
                    if (gp == null) {
                        // A resource or production instead
                        TMTypes.Resource res = TMTypes.Resource.valueOf(((CounterRequirement)r).counterCode.split("prod")[0]);
                        imgStr = res.getImagePath();
                    } else {
                        imgStr = gp.getImagePath();
                    }
                    Image from = ImageIO.GetInstance().getImage(imgStr);
                    drawImage(g, from, xD, yD, size);
                    drawShadowStringCentered(g, ": +/-" + card.discountEffects.get(r), new Rectangle(xD + size, yD, size*3, size));
                }
                yD += size + spacing/2;
            }
            // Draw resource mappings
            int yRM = yD + spacing;
            int xRM = x + width/2 - size*7/2;
            for (TMGameState.ResourceMapping rm: card.resourceMappings) {
                Image from = ImageIO.GetInstance().getImage(rm.from.getImagePath());
                Image to = ImageIO.GetInstance().getImage(rm.to.getImagePath());
                drawImage(g, from, xRM, yRM, size, size);
                drawImage(g, to, xRM + size * 5, yRM, size, size);
                drawShadowStringCentered(g, ": " + rm.rate, new Rectangle(xRM + size, yRM, size*4, size));
                yRM += size + spacing/2;
            }
            // Draw after-action effects
            int yEF = yRM + spacing;
            int xEF = x + width/2 - size*5/2;
            for (Effect e: card.persistingEffects) {
                int leftNumber = -1;
                int rightNumber = -1;
                Image from = null;
                Image to = null;
                if (e instanceof PayForActionEffect) {
                    from = ImageIO.GetInstance().getImage(TMTypes.Resource.MegaCredit.getImagePath());
                    leftNumber = ((PayForActionEffect) e).minCost;
                } else if (e instanceof PlaceTileEffect) {
                    if (((PlaceTileEffect) e).tile != null) {
                        from = ImageIO.GetInstance().getImage(((PlaceTileEffect) e).tile.getImagePath());
                    } else if (((PlaceTileEffect) e).resourceTypeGained != null) {
                        // several images separated by slash
                        int nRes = ((PlaceTileEffect) e).resourceTypeGained.length;
                        int sectionSize = (nRes-1) * size * 2;
                        int resX = xEF + size - sectionSize;
                        int count = 0;
                        for (TMTypes.Resource r: ((PlaceTileEffect) e).resourceTypeGained) {
                            Image imgR = ImageIO.GetInstance().getImage(r.getImagePath());
                            drawResource(g, imgR, production, false, resX, yEF, size, 0.6);
                            resX += size;
                            if (count != nRes-1) {
                                drawShadowStringCentered(g, "/", new Rectangle(resX, yEF, size, size), Color.white, Color.black, 12);
                                resX += size;
                            }
                            count++;
                        }
                    } else {
                        from = ImageIO.GetInstance().getImage(TMTypes.Tile.City.getImagePath());
                    }
                } else if (e instanceof PlayCardEffect) {
                    from = ImageIO.GetInstance().getImage(((PlayCardEffect) e).tagOnCard.getImagePath());
                }
                // "to" depends on the action applied as the effect
                TMAction action = e.effectAction;
                if (action == null) {
                    action = TMAction.parseAction(e.effectEncoding, true).a;
                }
                if (action instanceof PlaceholderModifyCounter) {
                    drawPlaceHolderCounterActionMiddle(g, (PlaceholderModifyCounter) action, xEF + size * 4, yEF, size);
                }

                if (!e.mustBeCurrentPlayer) {
                    // draw red outline for the left part
                    g.setColor(anyPlayerColor);
                    int xRect = xEF;
                    int widthRect = size * 2;
                    if (leftNumber == -1) {
                        xRect = xEF + size;
                        widthRect = size;
                    }
                    g.fillRoundRect(xRect - 2, yEF - 2, widthRect + 4, size + 4, spacing, spacing);
                }

                if (leftNumber != -1) {
                    drawShadowStringCentered(g, "" + leftNumber, new Rectangle(xEF, yEF, size, size), Color.white, Color.black, 12);
                }
                if (from != null) {
                    drawImage(g, from, xEF + size, yEF, size, size);
                }
                drawShadowStringCentered(g, " : ", new Rectangle(xEF + size*2, yEF, size, size));
                if (rightNumber != -1) {
                    drawShadowStringCentered(g, "" + rightNumber, new Rectangle(xEF + size * 3, yEF, size, size), Color.white, Color.black, 12);
                }
                if (to != null) {
                    drawImage(g, to, xEF + size * 4, yEF, size, size);
                }
                yEF += size + spacing/2;
            }
        } else {
            // Draw background
            drawImage(g, projCardBg, x, y, height);
            // Draw ribbon
            Image ribbon = ImageIO.GetInstance().getImage(card.cardType.getImagePath());
            Rectangle ribbonRect = drawImage(g, ribbon, x + 2, y + height/8 - 2, width - 4);
            // Draw name
            Font f = g.getFont();
            g.setFont(new Font("Arial", Font.BOLD, 14));
            drawStringCentered(g, card.getComponentName(), ribbonRect, Color.black, 14);
            g.setFont(f);
            // Draw cost
            drawStringCentered(g, "" + card.cost, new Rectangle(x, y, (int)(width/5.5), (int)(width/5.5)), Color.darkGray, 14);
            // Draw points
            if (card.nPoints != 0) {
                Vector2D dim = scaleLargestDimImg(pointBg, defaultItemSize);
                drawImage(g, pointBg, x + width - dim.getX() - 2, y + height - dim.getY() - 2, dim.getX(), dim.getY());
                Rectangle contentRect = new Rectangle(x + width - dim.getX() - 2, y + height - dim.getY() - 2, dim.getX(), dim.getY());
                int size = contentRect.width/3;
                int nOther = (int)(1/card.nPoints);
                if (card.pointsResource != null) {
                    drawShadowStringCentered(g, "1/" + (nOther != 1? nOther : ""),
                            new Rectangle(contentRect.x, contentRect.y, contentRect.width/2, contentRect.height),
                            Color.orange, Color.black, 14);
                    drawImage(g, ImageIO.GetInstance().getImage(card.pointsResource.getImagePath()),
                            contentRect.x + contentRect.width/2 + contentRect.width/4 - size/2, contentRect.y + contentRect.height/2 - size/2,
                            size, size);
                } else if (card.pointsTile != null) {
                    drawShadowStringCentered(g, "1/" + (nOther != 1? nOther : ""),
                            new Rectangle(contentRect.x, contentRect.y, contentRect.width/2, contentRect.height),
                            Color.orange, Color.black, 14);
                    drawImage(g, ImageIO.GetInstance().getImage(card.pointsTile.getImagePath()),
                            contentRect.x + contentRect.width/2 + contentRect.width/4 - size/2, contentRect.y + contentRect.height/2 - size/2,
                            size, size);
                } else if (card.pointsTag != null) {
                    drawShadowStringCentered(g, "1/" + (nOther != 1? nOther : ""),
                            new Rectangle(contentRect.x, contentRect.y, contentRect.width/2, contentRect.height),
                            Color.orange, Color.black, 14);
                    drawImage(g, ImageIO.GetInstance().getImage(card.pointsTag.getImagePath()),
                            contentRect.x + contentRect.width/2 + contentRect.width/4 - size/2, contentRect.y + contentRect.height/2 - size/2,
                            size, size);
                } else {
                    drawShadowStringCentered(g, "" + (int)card.nPoints, contentRect, Color.orange);
                }
            }
            // Draw tags
            int tagSize = defaultItemSize/3;
            int tagsWidth = card.tags.length * tagSize;
            int startX = (int)(aboveRibbon.getX() + aboveRibbon.getWidth() - tagsWidth);
            int tagY = (int)(aboveRibbon.getY() + aboveRibbon.getHeight()/2 - tagSize/2);
            for (int i = 0; i < card.tags.length; i++) {
                TMTypes.Tag tag = card.tags[i];
                Image img = ImageIO.GetInstance().getImage(tag.getImagePath());
                drawImage(g, img, startX + i*tagSize, tagY, tagSize, tagSize);
            }
            // Draw requirements
            if (card.requirements.size() > 0) {
                Rectangle reqRect = new Rectangle(aboveRibbon.x + spacing/2, aboveRibbon.y + 2, aboveRibbon.width-tagsWidth - spacing, aboveRibbon.height - 4);
                boolean max = false;
                for (Requirement r: card.requirements) {
                    if (r.isMax()) max = true;
                }
                if (max) {
                    drawImage(g, reqMax, reqRect.x, reqRect.y, reqRect.width, reqRect.height);
                } else {
                    drawImage(g, reqMin, reqRect.x, reqRect.y, reqRect.width, reqRect.height);
                }
                int sX = reqRect.x + spacing;
                int sY = reqRect.y + spacing/5;
                for (Requirement r: card.requirements) {
                    String text = r.getDisplayText(gs);
                    Image[] imgs = r.getDisplayImages();
                    if (text != null) {
                        drawShadowString(g, text, sX, sY);
                        FontMetrics fm = g.getFontMetrics();
                        sX += fm.stringWidth(text);
                    }
                    if (imgs != null) {
                        for (Image img: imgs) {
                            drawImage(g, img, sX, sY, tagSize, tagSize);
                            sX += tagSize;
                        }
                    }
                }
            }
            // Draw card effects
            if (card.immediateEffects.length > 0) {
                int yE = ribbonRect.y + ribbonRect.height + spacing;
                int xE = x + width/2;
                for (TMAction a: card.immediateEffects) {
                    yE = drawCardEffect(g, a, xE, yE);
                }
            }
        }
    }

    static void drawResource(Graphics2D g, Image resImg, Image production, boolean prod, int x, int y, int size, double scaleResIfProd) {
        if (prod) {
            drawImage(g, production, x, y, size);
            int newSize = (int)(size * scaleResIfProd);
            x += size/2 - newSize/2;
            y += size/2 - newSize/2;
            size = newSize;
        }
        drawImage(g, resImg, x, y, size);
    }

    private int drawCardEffect(Graphics2D g, TMAction a, int xE, int yE) {
        // xE is the middle

        if (a instanceof PlaceholderModifyCounter) {
            drawPlaceHolderCounterActionMiddle(g, (PlaceholderModifyCounter) a, xE, yE, defaultItemSize/3);
            yE += defaultItemSize/3 + spacing/5;
        } else if (a instanceof PlaceTile) {
            drawPlaceTileAction(g, (PlaceTile) a, xE, yE, defaultItemSize/2);
            yE += defaultItemSize/2 + spacing/5;
        } else if (a instanceof AddResourceOnCard) {
            drawAddResourceAction(g, (AddResourceOnCard) a, xE, yE, defaultItemSize/3);
            yE += defaultItemSize/3 + spacing/5;
        } else if (a instanceof ActionChoice) {
            // Horizontal display, each action next to each other, separated by /
            int nActions = ((ActionChoice) a).actions.length;
            int sepWidth = g.getFontMetrics().stringWidth(" / ");
            int widthOne = (cardWidth - sepWidth*(nActions-1))/nActions;
            xE = xE - cardWidth/2 + widthOne/2;
            for (int i = 0; i < nActions; i++) {
                drawCardEffect(g, ((ActionChoice) a).actions[i], xE, yE);
                xE += widthOne;
                if (i != nActions-1) {
                    drawShadowStringCentered(g, " / ", new Rectangle(xE - widthOne/2, yE, sepWidth, defaultItemSize / 2));
                    xE += sepWidth;
                }
            }
            yE += defaultItemSize/2 + spacing/5;
        } else if (a instanceof CompoundAction) {
            // Vertical display, one action under the other
            int nActions = ((CompoundAction) a).actions.length;
            for (int i = 0; i < nActions; i++) {
                yE = drawCardEffect(g, ((CompoundAction) a).actions[i], xE, yE);
            }
        } else {
            int b = 0;
        }
        return yE;
    }

    private void drawAddResourceAction(Graphics2D g, AddResourceOnCard a, int x, int y, int size) {
        // x is the middle

        String amount = "" + a.amount;
        TMTypes.Resource res = a.resource;
        boolean any = a.chooseAny;
        boolean another = a.cardID == -1;
        TMTypes.Tag tagRequired = a.tagRequirement;
        int minResRequired = a.minResRequirement;

        // Calculate width of display
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(amount) + size + 4;
        if (another) {
            if (!any) {
                // *
                w += fm.stringWidth("* ");
            }
            if (tagRequired != null || minResRequired > 0) {
                w += fm.stringWidth("()");
                if (tagRequired != null) {
                    // (tag)
                    w += size;
                }
                if (minResRequired > 0) {
                    // (min: X)
                    w += fm.stringWidth("min: " + minResRequired);
                }
            }
        }
        x -= w/2;

        // Draw amount
        drawShadowStringCentered(g, amount, new Rectangle(x, y, fm.stringWidth(amount), size));
        x += fm.stringWidth(amount);
        if (any) {
            // red background
            g.setColor(anyPlayerColor);
            g.fillRoundRect(x, y - 2, size + 4, size + 4, spacing, spacing);
        }
        // Draw res
        drawImage(g, ImageIO.GetInstance().getImage(res.getImagePath()), x + 2, y, size, size);
        x += size + 4;
        // Draw other decorators
        if (another) {
            if (!any) {
                // *
                drawShadowStringCentered(g, "* ", new Rectangle(x, y, fm.stringWidth("* "), size));
                x += fm.stringWidth("* ");
            }
            if (tagRequired != null || minResRequired > 0) {
                // (something)
                drawShadowStringCentered(g, "(", new Rectangle(x, y, fm.stringWidth("("), size));
                x += fm.stringWidth("(");

                if (tagRequired != null) {
                    drawImage(g, ImageIO.GetInstance().getImage(tagRequired.getImagePath()), x, y, size, size);
                    x += size;
                }
                if (minResRequired > 0) {
                    // (min: X)
                    String minText = "min: " + minResRequired;
                    drawStringCentered(g, minText, new Rectangle(x, y, fm.stringWidth(minText), size), Color.gray);
                    x += fm.stringWidth(minText);
                }

                drawShadowStringCentered(g, ")", new Rectangle(x, y, fm.stringWidth(")"), size));
            }
        }
    }

    private void drawPlaceTileAction(Graphics2D g, PlaceTile a, int x, int y, int size) {
        // x is the middle

        TMTypes.Tile t = a.tile;
        TMTypes.MapTileType mt = a.mapType;
        String name = a.tileName;
        int w = size;
        String sep = " -> ";
        FontMetrics fm = g.getFontMetrics();
        int sepW = fm.stringWidth(sep);
        if (mt != null && t.getRegularLegalTileType() != mt) {
            // Draw special placement requirement
            int tileW = fm.stringWidth(mt.name());
            w += tileW + sepW + spacing*2/5;
        }
        if (name != null) {
            // Draw location name
            int nameW = fm.stringWidth(name);
            w += nameW + sepW + spacing*2/5;
        }
        // Do actual drawing
        int xE2 = x - w/2;
        drawImage(g, t.getImagePath(), xE2, y, size, size);
        if (mt != null && t.getRegularLegalTileType() != mt) {
            // Draw special placement requirement
            drawStringCentered(g, " -> " + mt.name(),
                    new Rectangle(xE2 + size, y, sepW + fm.stringWidth(mt.name()), size), Color.gray);
        }
        if (name != null) {
            // Draw location name
            drawStringCentered(g, " -> " + name,
                    new Rectangle(xE2 + size, y, sepW + fm.stringWidth(name), size), Color.gray);
        }
    }

    private void drawPlaceHolderCounterAction(Graphics2D g, PlaceholderModifyCounter aa, int x, int y, int size) {
        // x is the left of this rectangle

        TMTypes.Resource res = aa.resource;
        String amount = "" + aa.change;
        boolean prod = aa.production;
        Image resImg = ImageIO.GetInstance().getImage(res.getImagePath());
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(amount);

        drawResource(g, resImg, production, prod, x, y, size, 0.6);
        drawShadowStringCentered(g, amount, new Rectangle(x + size + spacing/5, y, textWidth, size));
    }

    private void drawPlaceHolderCounterActionMiddle(Graphics2D g, PlaceholderModifyCounter aa, int x, int y, int size) {
        // x is the middle of this rectangle
        String amount = "" + aa.change;
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(amount);
        int totalWidth = size + spacing/5 + textWidth;
        x -= totalWidth/2;

        drawPlaceHolderCounterAction(g, aa, x, y, size);
    }

    private void drawAction(Graphics2D g, TMAction a, int x, int y, int size) {
        int leftNumber = -1;
        String left = null;
        String right = null;
        int rightNumber = -1;
        if (a instanceof PayForAction) {
            PayForAction aa = (PayForAction) a;
            TMTypes.Resource leftR = aa.resourceToPay;
            left = leftR.getImagePath();
            leftNumber = Math.abs(aa.costTotal);
            boolean played = aa.played;
            if (aa.action instanceof PlaceTile) {
                // get the tile image
                TMTypes.Tile t = ((PlaceTile)aa.action).tile;
                right = t.getImagePath();
            } else if (aa.action instanceof ResourceTransaction) {
                // get resource image
                TMTypes.Resource rightR = ((ResourceTransaction)aa.action).res;
                right = rightR.getImagePath();
            }
        }
        // Draw left + arrow + right
        if (leftNumber != -1) x -= size/2;
        if (rightNumber != -1) x -= size/2;
        if (left != null) x -= size/2;
        if (right != null) x -= size/2;

        if (leftNumber != -1) {
            drawShadowStringCentered(g, "" + leftNumber, new Rectangle(x, y, size, size), Color.white, Color.black, 12);
            x += size;
        }
        if (left != null) {
            Image image = ImageIO.GetInstance().getImage(left);
            drawImage(g, image, x, y, size, size);
            x += size;
        }
        drawImage(g, actionArrow, x + size, y, defaultItemSize/2, size);
        x += defaultItemSize/2 + size*2;
        if (rightNumber != -1) {
            drawShadowStringCentered(g, "" + rightNumber, new Rectangle(x, y, size, size), Color.white, Color.black, 12);
            x += size;
        }
        if (right != null) {
            Image image = ImageIO.GetInstance().getImage(right);
            drawImage(g, image, x, y, size, size);
        }
    }


    public ArrayList<Rectangle> getHighlight() {
        return highlight;
    }

    public void update(Deck<TMCard> deck) {
        this.deck = deck;
        if (deck != null) {
            width = deck.getSize() * cardWidth;
            revalidate();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }
}