package moze_intel.projecte.gameObjs.gui;

import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.manual.AbstractPage;
import moze_intel.projecte.manual.ImagePage;
import moze_intel.projecte.manual.ItemPage;
import moze_intel.projecte.manual.ManualFontRenderer;
import moze_intel.projecte.manual.ManualPageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GUIManual extends GuiScreen
{
    public static final int WINDOW_WIDTH = 256;
    public static final int TEXT_WIDTH = 145;
    public static final int PAGE_HEIGHT = 226;
    public static final int TEXT_HEIGHT = PAGE_HEIGHT - 43 - 20;
    public static final int TEXT_Y_OFFSET = 10;
    public static final float GUI_SCALE_FACTOR = 1.5f;
    public static final int BUTTON_HEIGHT = 13;
    private static final int CHARACTER_HEIGHT = 9;
    private static final int BUTTON_ID_OFFSET = 3;
    private static final ResourceLocation bookTexture = new ResourceLocation("projecte:textures/gui/bookTexture.png");
    private static final ResourceLocation tocTexture = new ResourceLocation("projecte:textures/gui/bookTexture.png");
    private static final ManualFontRenderer peFontRenderer = new ManualFontRenderer();
    private static ResourceLocation bookGui = new ResourceLocation("textures/gui/book.png");
    public List<String> bodyTexts = Lists.newArrayList();
    private int indexPages = 1;
    private int currentPageID;
    private int indexPageID = 0;
    private int entriesPerPage = 0;

    public static void drawItemStackToGui(Minecraft mc, ItemStack item, int x, int y, boolean fixLighting)
    {
        if (fixLighting)
        {
            GL11.glEnable(GL11.GL_LIGHTING);
        }

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        itemRender.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), item, x, y);

        if (fixLighting)
        {
            GL11.glDisable(GL11.GL_LIGHTING);
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    @Override
    public void initGui()
    {
        indexPageID = 0;
        ScaledResolution scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        width = scaledresolution.getScaledWidth();

        int i = (this.width - WINDOW_WIDTH) / 2;

        this.buttonList.add(new PageTurnButton(0, Math.round((i + 210) * (GUI_SCALE_FACTOR * 0.75f)), PAGE_HEIGHT - Math.round(BUTTON_HEIGHT * 1.2f), true));
        this.buttonList.add(new PageTurnButton(1, Math.round((i + 16) / GUI_SCALE_FACTOR), PAGE_HEIGHT - Math.round(BUTTON_HEIGHT * 1.2f), false));

        String text = StatCollector.translateToLocal("pe.manual.index_button");
        int stringWidth = mc.fontRenderer.getStringWidth(text);

        this.buttonList.add(new TocButton(2, (this.width / 2) - (stringWidth / 2), PAGE_HEIGHT - Math.round(BUTTON_HEIGHT * 1.3f), stringWidth, 15, text));

        entriesPerPage = TEXT_HEIGHT / CHARACTER_HEIGHT - 2;
        indexPages = MathHelper.ceiling_float_int(((float) ManualPageHandler.pages.size()) / entriesPerPage);

        addIndexButtons(((Math.round(this.width / GUI_SCALE_FACTOR) - WINDOW_WIDTH) / 2) + 40);

        indexPageID -= indexPages;
        currentPageID = indexPageID;

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (isViewingIndex())
        {
            this.mc.getTextureManager().bindTexture(tocTexture);
        } else
        {
            this.mc.getTextureManager().bindTexture(bookTexture);
        }

        GL11.glScalef(GUI_SCALE_FACTOR, 1, GUI_SCALE_FACTOR);
        int k = (Math.round(this.width / GUI_SCALE_FACTOR) - WINDOW_WIDTH) / 2;
        this.drawTexturedModalRect(k, 5, 0, 0, WINDOW_WIDTH, PAGE_HEIGHT);
        GL11.glScalef(1 / GUI_SCALE_FACTOR, 1, 1 / GUI_SCALE_FACTOR);

        if (!isViewingIndex())
        {
            AbstractPage currentPage = ManualPageHandler.pages.get(currentPageID);
            AbstractPage nextPage = currentPageID == ManualPageHandler.pages.size() || currentPageID == ManualPageHandler.pages.size() - 1 ? null : ManualPageHandler.pages.get(currentPageID + 1); // todo clean

            if (currentPage != null)
                drawPage(currentPage, k + 40, k + 20);
            if (nextPage != null)
                drawPage(nextPage, k + 160, k + 140);
        } else
        {
            this.fontRendererObj.drawString(StatCollector.translateToLocal("pe.manual.index"), k + 60, 27, 0, false);
        }

        this.updateButtons();

        for (GuiButton button : ((List<GuiButton>) this.buttonList))
        {
            button.drawButton(this.mc, mouseX, mouseY);
        }

    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        switch (button.id)
        {
            case 0:
                if (currentPageID != -1)
                    currentPageID += 2;
                else
                    currentPageID++;
                break;
            case 1:
                if (currentPageID != 0)
                    currentPageID -= 2;
                else
                    currentPageID--;
                break;
            case 2:
                currentPageID = indexPageID;
                break;
            default:
                currentPageID = button.id - 3 - ((button.id - 3) % 2);
        }
        this.updateButtons();
    }

    private void updateButtons()
    {
        if (isViewingIndex())
        {
            ((PageTurnButton) this.buttonList.get(0)).visible = true;
            if (currentPageID != indexPageID)
                ((PageTurnButton) this.buttonList.get(1)).visible = true;
            else
                ((PageTurnButton) this.buttonList.get(1)).visible = false;
            ((TocButton) this.buttonList.get(2)).visible = false;
            for (int i = 3; i < this.buttonList.size(); i++)
            {
                if (i > (entriesPerPage * ((indexPages + 1 - Math.abs(currentPageID)) - 1)) + BUTTON_ID_OFFSET &&
                        i <= (entriesPerPage * (indexPages + 1 - Math.abs(currentPageID + 1))) + BUTTON_ID_OFFSET)
                {
                    ((IndexLinkButton) this.buttonList.get(i)).visible = true;
                } else
                    ((IndexLinkButton) this.buttonList.get(i)).visible = false;
            }
        } else if (this.currentPageID >= ManualPageHandler.pages.size() - 2)
        {
            ((PageTurnButton) this.buttonList.get(0)).visible = false;
            ((PageTurnButton) this.buttonList.get(1)).visible = true;
            ((TocButton) this.buttonList.get(2)).visible = true;
            for (int i = 3; i < this.buttonList.size(); i++)
            {
                ((IndexLinkButton) this.buttonList.get(i)).visible = false;
            }
        } else
        {
            ((PageTurnButton) this.buttonList.get(0)).visible = true;
            ((PageTurnButton) this.buttonList.get(1)).visible = true;
            ((TocButton) this.buttonList.get(2)).visible = true;
            for (int i = 3; i < this.buttonList.size(); i++)
            {
                ((IndexLinkButton) this.buttonList.get(i)).visible = false;
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    public void drawImage(ResourceLocation resource, int x, int y)
    {
        Minecraft.getMinecraft().renderEngine.bindTexture(resource);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glScalef(0.5F, 0.5F, 1F);
        this.drawTexturedModalRect(x, y, 0, 0, 256, 256);
        GL11.glScalef(2F, 2F, 1F);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void addIndexButtons(int x)
    {
        int yOffset = 42;
        int side = 0;
        int sideWas = 0;
        int skipped = 0;
        x *= GUI_SCALE_FACTOR;

        for (AbstractPage page : ManualPageHandler.pages)
        {
            if (!page.shouldAppearInIndex())
            {
                skipped++;
                continue;
            }

            if (side != sideWas)
            {
                yOffset = 42;
                if (side == 1)
                    x += 160 * GUI_SCALE_FACTOR;
                else
                    x -= 160 * GUI_SCALE_FACTOR;
                sideWas = side;
            }

            String text = page.getHeaderText();
            int buttonID = ManualPageHandler.pages.indexOf(page) + BUTTON_ID_OFFSET;
            addIndexButton(buttonID, x, yOffset, text);
            yOffset += CHARACTER_HEIGHT + 1;
            side = ((ManualPageHandler.pages.indexOf(page) - skipped) / (entriesPerPage)) % 2;
        }

    }

    private boolean isViewingIndex()
    {
        return currentPageID < 0;
    }

    private void addIndexButton(int buttonID, int x, int yOffset, String text)
    {
        buttonList.add(new IndexLinkButton(buttonID, Math.round((x * GUI_SCALE_FACTOR) / 2), yOffset, mc.fontRenderer.getStringWidth(text),
                CHARACTER_HEIGHT, text));
    }

    // Header = k+40, k+160, Image/Text = k+20, k+140
    public void drawPage(AbstractPage page, int headerX, int contentX)
    {
        this.fontRendererObj.drawString(page.getHeaderText(), Math.round(headerX * GUI_SCALE_FACTOR), 27, 0, false);

        if (page instanceof ImagePage)
        {
            drawImage(((ImagePage) page).getImageLocation(), Math.round(contentX * GUI_SCALE_FACTOR * 2), 80);
        } else
        {
            bodyTexts = splitBody(page);

            for (int i = 0; i < bodyTexts.size() && i < Math.floor(GUIManual.TEXT_HEIGHT / GUIManual.TEXT_Y_OFFSET); i++)
            {
                this.fontRendererObj.drawString(bodyTexts.get(i).charAt(0) == 32 ? bodyTexts.get(i).substring(1) : bodyTexts.get(i),
                        Math.round(contentX * GUI_SCALE_FACTOR), 43 + TEXT_Y_OFFSET * i, Color.black.getRGB());
            }

            if (page instanceof ItemPage)
            {
                ItemPage itemPage = ((ItemPage) page);
                drawItemStackToGui(mc, itemPage.getItemStack(), Math.round(contentX * GUI_SCALE_FACTOR), 22, !(itemPage.getItemStack().getItem() instanceof ItemBlock)
                        || itemPage.getItemStack().getItem() == Item.getItemFromBlock(ObjHandler.confuseTorch));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private static class TocButton extends GuiButton
    {
        public TocButton(int ID, int xPos, int yPos, int bWidth, int bHeight, String text)
        {
            super(ID, xPos, yPos, bWidth, bHeight, text);
        }

    }

    @SideOnly(Side.CLIENT)
    private static class IndexLinkButton extends GuiButton
    {
        public IndexLinkButton(int par1, int par2, int par3, int par4, int par5, String par6)
        {
            super(par1, par2, par3, par4, par5, par6);
        }

        @Override
        public void drawButton(Minecraft mc, int par2, int par3)
        {
            if (visible)
            {
                drawRect(xPosition, yPosition, (xPosition + width), (yPosition + height), 0);
                mc.fontRenderer.drawString(displayString, Math.round(xPosition), Math.round(yPosition), 0);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private static class PageTurnButton extends GuiButton
    {
        private static final int bWidth = 23;
        private boolean pointsRight;

        public PageTurnButton(int ID, int xPos, int yPos, boolean par4)
        {
            super(ID, xPos, yPos, bWidth, BUTTON_HEIGHT, "");
            pointsRight = par4;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY)
        {
            if (this.visible)
            {
                boolean hover = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(bookGui);
                int u = 0;
                int v = 192;

                if (hover)
                {
                    u += bWidth;
                }

                if (!pointsRight)
                {
                    v += BUTTON_HEIGHT;
                }
                GL11.glEnable(GL11.GL_BLEND);
                this.drawTexturedModalRect(this.xPosition, this.yPosition, u, v, bWidth, BUTTON_HEIGHT);
                GL11.glDisable(GL11.GL_BLEND);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> splitBody(AbstractPage page)
    {
        return peFontRenderer.listFormattedStringToWidth(page.getBodyText(), TEXT_WIDTH);
    }
}