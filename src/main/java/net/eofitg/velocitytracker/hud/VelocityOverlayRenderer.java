package net.eofitg.velocitytracker.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class VelocityOverlayRenderer {

    public static boolean enabled;        // Toggle for UI
    public static float scale;            // pixels per BPS (for XZ plane)
    public static int alpha;              // 0–255 UI opacity
    public static int graphSize;          // radius (pixels)
    public static int posX;               // top-left X
    public static int posY;               // top-left Y
    public static float vyRange;          // vertical bar range (±BPS)

    private final Minecraft mc = Minecraft.getMinecraft();

    // Trail history for horizontal velocity (X,Z) — unit: BPS
    private final Deque<float[]> trail = new ArrayDeque<>();
    private static final int MAX_TRAIL = 60; // 3 seconds @ 20tps

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post e) {
        if (!enabled) return;
        if (e.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.thePlayer == null) return;

        int baseX = posX;
        int baseY = posY;
        int size  = graphSize;
        int cx = baseX + size; // circle center X
        int cy = baseY + size; // circle center Y
        int diameter = size * 2;

        // Velocity (converted to blocks per second)
        double vx_bps = mc.thePlayer.motionX * 20.0;
        double vy_bps = mc.thePlayer.motionY * 20.0;
        double vz_bps = mc.thePlayer.motionZ * 20.0;

        // Push trail (X,Z only)
        pushTrail((float) vx_bps, (float) vz_bps);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.disableDepth();

        // Background panel
        drawFilledRect(baseX, baseY, baseX + diameter + 60, baseY + diameter, rgba(0, 0, 0, alpha / 3));

        int a = alpha;

        // XZ-plane axes
        drawCircleOutline(cx, cy, size, rgba(255, 255, 255, a / 3), 64);
        drawLine(cx - size, cy, cx + size, cy, rgba(200, 200, 200, a)); // X-axis
        drawLine(cx, cy - size, cx, cy + size, rgba(200, 200, 200, a)); // Z-axis

        // Scale ticks (1 BPS per grid)
        float pxPerBps = scale;
        int maxTicks = (int) Math.floor(size / pxPerBps);
        for (int i = 1; i <= maxTicks; i++) {
            int dx = Math.round(i * pxPerBps);
            int color = rgba(180, 180, 180, a / 2);
            drawLine(cx - dx, cy - 3, cx - dx, cy + 3, color);
            drawLine(cx + dx, cy - 3, cx + dx, cy + 3, color);
            drawLine(cx - 3, cy - dx, cx + 3, cy - dx, color);
            drawLine(cx - 3, cy + dx, cx + 3, cy + dx, color);
        }

        // Trail (old → new)
        float[] last = null;
        for (float[] v : trail) {
            int color = rgba(100, 200, 255, (int) (a * 0.35f));
            float x = cx + v[0] * pxPerBps;
            float y = cy + v[1] * pxPerBps; // Z is mapped directly (right-handed)
            if (last != null) {
                float lx = cx + last[0] * pxPerBps;
                float ly = cy + last[1] * pxPerBps;
                drawLine(lx, ly, x, y, color);
            }
            last = v;
        }

        // Current velocity arrow (X,Z)
        float tipX = (float) (cx + vx_bps * pxPerBps);
        float tipY = (float) (cy + vz_bps * pxPerBps);

        // Constrain inside circle
        float dx = tipX - cx, dy = tipY - cy;
        float dist = (float) Math.sqrt(dx*dx + dy*dy);
        if (dist > size - 5) {
            float k = (size - 5) / dist;
            tipX = cx + dx * k;
            tipY = cy + dy * k;
        }

        drawLine(cx, cy, tipX, tipY, rgba(80, 255, 120, a));
        drawArrowHead(cx, cy, tipX, tipY, 8f, rgba(80, 255, 120, a));

        // ---------- Vertical bar (Vy) ----------
        int barX = baseX + diameter + 30; // space on the right
        int barY = baseY;
        int barHeight = diameter;
        int barCenter = barY + size;

        // Background bar
        drawFilledRect(barX - 6, barY, barX + 6, barY + barHeight, rgba(0, 0, 0, alpha / 4));
        drawLine(barX - 10, barCenter, barX + 10, barCenter, rgba(200, 200, 200, a)); // Vy=0 line

        // Map Vy to pixels
        float pixelsPerBpsY = (float) size / vyRange;
        int vyPos = (int) (barCenter - vy_bps * pixelsPerBpsY);
        if (vyPos < barY + 2) vyPos = barY + 2;
        if (vyPos > barY + barHeight - 2) vyPos = barY + barHeight - 2;

        // Indicator
        drawFilledRect(barX - 5, vyPos - 3, barX + 5, vyPos + 3, rgba(255, 120, 80, a));

        // ---------- Text label ----------
        GlStateManager.enableTexture2D();
        String label = String.format("Vx=%.2f  Vy=%.2f  Vz=%.2f BPS", vx_bps, vy_bps, vz_bps);
        mc.fontRendererObj.drawStringWithShadow(label, baseX + 4, baseY + diameter + 4, 0xFFFFFF);

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void pushTrail(float vx, float vz) {
        if (trail.size() >= MAX_TRAIL) trail.removeFirst();
        trail.addLast(new float[]{vx, vz});
    }

    /* ---------- Primitive rendering ---------- */

    private void drawLine(float x1, float y1, float x2, float y2, int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        GlStateManager.color(r, g, b, a);
        GL11.glLineWidth(1.5f);
        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        wr.pos(x1, y1, 0).endVertex();
        wr.pos(x2, y2, 0).endVertex();
        tess.draw();
    }

    private void drawFilledRect(int x1, int y1, int x2, int y2, int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        GlStateManager.color(r, g, b, a);
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        wr.pos(x1, y2, 0).endVertex();
        wr.pos(x2, y2, 0).endVertex();
        wr.pos(x2, y1, 0).endVertex();
        wr.pos(x1, y1, 0).endVertex();
        tess.draw();
    }

    private void drawCircleOutline(int cx, int cy, int radius, int color, int steps) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        GlStateManager.color(r, g, b, a);
        GL11.glLineWidth(1.0f);
        wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        for (int i = 0; i < steps; i++) {
            double t = (Math.PI * 2 * i) / steps;
            wr.pos(cx + Math.cos(t) * radius, cy + Math.sin(t) * radius, 0).endVertex();
        }
        tess.draw();
    }

    private void drawArrowHead(float x0, float y0, float x1, float y1, float size, int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        float dx = x1 - x0, dy = y1 - y0;
        float len = (float) Math.sqrt(dx*dx + dy*dy);
        if (len < 0.001f) return;
        float ux = dx / len, uy = dy / len;

        // Two wing points
        float leftX = x1 - ux * size - uy * (size * 0.6f);
        float leftY = y1 - uy * size + ux * (size * 0.6f);
        float rightX = x1 - ux * size + uy * (size * 0.6f);
        float rightY = y1 - uy * size - ux * (size * 0.6f);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        GlStateManager.color(r, g, b, a);
        wr.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
        wr.pos(x1, y1, 0).endVertex();
        wr.pos(leftX, leftY, 0).endVertex();
        wr.pos(rightX, rightY, 0).endVertex();
        tess.draw();
    }

    private int rgba(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }
}
