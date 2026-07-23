package org.catrobat.catroid.utils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.Vector;

public class GifDecoder {
    private static class GifFrame {
        public Pixmap image;
        public int delay;
        public GifFrame(Pixmap im, int del) {
            image = im;
            delay = del;
        }
    }

    public static Animation<TextureRegion> loadGIFAnimation(Animation.PlayMode playMode, InputStream is) {
        GifDecoder decoder = new GifDecoder();
        int err = decoder.read(is);
        if (err != 0 || decoder.getFrameCount() <= 0) return null;

        Array<TextureRegion> frames = new Array<>();
        float totalDelay = 0;
        int frameCount = decoder.getFrameCount();

        for (int i = 0; i < frameCount; i++) {
            Pixmap pixmap = decoder.getFrame(i);
            if (pixmap != null) {
                Texture texture = new Texture(pixmap);
                TextureRegion region = new TextureRegion(texture);
                frames.add(region);
                int delay = decoder.getDelay(i);
                if (delay <= 0) delay = 100;
                totalDelay += delay / 1000f;
                pixmap.dispose();
            }
        }

        float frameDuration = totalDelay / frameCount;
        if (frameDuration <= 0) frameDuration = 0.1f;

        Animation<TextureRegion> animation = new Animation<>(frameDuration, frames);
        animation.setPlayMode(playMode);
        return animation;
    }

    private InputStream in;
    private int status;
    private int width, height;
    private boolean gctFlag;
    private int gctSize;
    private int lsdBgColor;
    private int[] gct;
    private int[] lct;
    private int[] act;
    private int bgIndex;
    private int pixelAspect;
    private boolean lctFlag;
    private int lctSize;
    private int ix, iy, iw, ih;
    private int lsdWidth, lsdHeight;
    private int transIndex;
    private int blockLength = 0;
    private final byte[] block = new byte[256];
    private final short[] prefix = new short[4096];
    private final byte[] suffix = new byte[4096];
    private final byte[] pixelStack = new byte[4097];
    private byte[] pixels;
    private final Vector<GifFrame> frames = new Vector<>();
    private boolean transColor;
    private int gceDelay;
    private int dispose = 0;

    private int[] masterCanvas;
    private int[] previousCanvas;
    private int lastDispose = 0;
    private int lastIx, lastIy, lastIw, lastIh;
    private boolean interlace = false;

    public int getDelay(int n) {
        gceDelay = -1;
        if ((n >= 0) && (n < frames.size())) {
            gceDelay = frames.elementAt(n).delay;
        }
        return gceDelay;
    }

    public int getFrameCount() {
        return frames.size();
    }

    public Pixmap getFrame(int n) {
        if (frames.size() <= 0) return null;
        return frames.elementAt(n).image;
    }

    public int read(InputStream is) {
        init();
        if (is != null) {
            in = is;
            readHeader();
            if (!err()) {
                readContents();
                if (getFrameCount() < 0) {
                    status = 1; // Error
                }
            }
        } else {
            status = 1;
        }
        try { is.close(); } catch (Exception ignored) {}
        return status;
    }

    private void init() {
        status = 0;
        frames.clear();
        gct = null;
        lct = null;
        masterCanvas = null;
        previousCanvas = null;
        lastDispose = 0;
        lastIx = 0; lastIy = 0; lastIw = 0; lastIh = 0;
    }

    private boolean err() {
        return status != 0;
    }

    private void readHeader() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 6; i++) id.append((char) readByte());
        if (!id.toString().startsWith("GIF")) {
            status = 1;
            return;
        }
        readLSD();
        if (gctFlag && !err()) {
            gct = readColorTable(gctSize);
        }
    }

    private void readLSD() {
        lsdWidth = readShort();
        lsdHeight = readShort();
        int packed = readByte();
        gctFlag = (packed & 0x80) != 0;
        gctSize = 2 << (packed & 7);
        bgIndex = readByte();
        pixelAspect = readByte();
        width = lsdWidth;
        height = lsdHeight;
    }

    private int readByte() {
        int curByte = 0;
        try {
            curByte = in.read();
        } catch (Exception e) {
            status = 1;
        }
        return curByte;
    }

    private int readShort() {
        return readByte() | (readByte() << 8);
    }

    private int[] readColorTable(int ncolors) {
        int nbytes = 3 * ncolors;
        byte[] c = new byte[nbytes];
        int n = 0;
        try {
            n = in.read(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (n < nbytes) {
            status = 1;
            return null;
        }
        int[] tab = new int[256];
        int i = 0;
        int j = 0;
        while (i < ncolors) {
            int r = c[j++] & 0xff;
            int g = c[j++] & 0xff;
            int b = c[j++] & 0xff;
            tab[i++] = 0xff000000 | (r << 16) | (g << 8) | b;
        }
        return tab;
    }

    private void readContents() {
        boolean terminate = false;
        while (!(terminate || err())) {
            int code = readByte();
            switch (code) {
                case 0x2C: // Image separator
                    readImage();
                    break;
                case 0x21: // Extension
                    int code2 = readByte();
                    switch (code2) {
                        case 0xF9: // Graphics control
                            readGCE();
                            break;
                        default:
                            skip();
                    }
                    break;
                case 0x3B: // Terminator
                    terminate = true;
                    break;
                case 0x00: // Bad byte
                default:
                    status = 1;
            }
        }
    }

    private void readGCE() {
        readByte();
        int packed = readByte();
        dispose = (packed & 0x1C) >> 2;
        if (dispose == 0) {
            dispose = 1;
        }
        transColor = (packed & 1) != 0;
        gceDelay = readShort() * 10;
        transIndex = readByte();
        readByte();
    }

    private void readImage() {
        ix = readShort();
        iy = readShort();
        iw = readShort();
        ih = readShort();
        int packed = readByte();
        lctFlag = (packed & 0x80) != 0;
        interlace = (packed & 0x40) != 0;
        lctSize = 2 << (packed & 7);
        if (lctFlag) {
            lct = readColorTable(lctSize);
            act = lct;
        } else {
            act = gct;
        }
        int save = 0;
        if (transColor && act != null && transIndex < act.length) {
            save = act[transIndex];
            act[transIndex] = 0;
        }
        if (act == null) {
            status = 1;
        }
        if (err()) return;
        decodeImageData();
        skip();
        if (err()) return;
        setPixmapFrame();
        if (transColor && act != null && transIndex < act.length) {
            act[transIndex] = save;
        }
    }

    private void decodeImageData() {
        int NullCode = -1;
        int npix = iw * ih;
        if ((pixels == null) || (pixels.length < npix)) {
            pixels = new byte[npix];
        }
        int data_size = readByte();
        int clear = 1 << data_size;
        int end_of_information = clear + 1;
        int available = clear + 2;
        int old_code = NullCode;
        int code_size = data_size + 1;
        int code_mask = (1 << code_size) - 1;
        for (int code = 0; code < clear; code++) {
            prefix[code] = 0;
            suffix[code] = (byte) code;
        }
        int datum = 0;
        int bits = 0;
        int first = 0;
        int top = 0;
        int pi = 0;
        int count = 0;
        int bi = 0;

        for (int i = 0; i < npix; ) {
            if (top == 0) {
                if (bits < code_size) {
                    if (count == 0) {
                        count = readBlock();
                        if (count <= 0) break;
                        bi = 0;
                    }
                    datum += (block[bi] & 0xff) << bits;
                    bits += 8;
                    bi++;
                    count--;
                    continue;
                }
                int code = datum & code_mask;
                datum >>= code_size;
                bits -= code_size;
                if ((code > available) || (code == end_of_information)) break;
                if (code == clear) {
                    code_size = data_size + 1;
                    code_mask = (1 << code_size) - 1;
                    available = clear + 2;
                    old_code = NullCode;
                    continue;
                }
                if (old_code == NullCode) {
                    pixelStack[top++] = suffix[code];
                    old_code = code;
                    first = code;
                    continue;
                }
                int in_code = code;
                if (code == available) {
                    pixelStack[top++] = (byte) first;
                    code = old_code;
                }
                while (code > clear) {
                    pixelStack[top++] = suffix[code];
                    code = prefix[code];
                }
                first = suffix[code] & 0xff;
                pixelStack[top++] = (byte) first;
                if (available < 4096) {
                    prefix[available] = (short) old_code;
                    suffix[available] = (byte) first;
                    available++;
                    if (((available & code_mask) == 0) && (available < 4096)) {
                        code_size++;
                        code_mask += available;
                    }
                }
                old_code = in_code;
            }
            top--;
            pixels[pi++] = pixelStack[top];
            i++;
        }
        for (int i = pi; i < npix; i++) {
            pixels[i] = 0;
        }
    }

    private int readBlock() {
        blockLength = readByte();
        int n = 0;
        if (blockLength > 0) {
            try {
                int count;
                while (n < blockLength) {
                    count = in.read(block, n, blockLength - n);
                    if (count == -1) break;
                    n += count;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (n < blockLength) {
                status = 1;
            }
        }
        return n;
    }

    private void skip() {
        do {
            readBlock();
        } while (blockLength > 0 && !err());
    }

    private void setPixmapFrame() {
        if (width <= 0 || height <= 0) return;

        if (masterCanvas == null || masterCanvas.length != width * height) {
            masterCanvas = new int[width * height];
            previousCanvas = new int[width * height];
        }

        if (lastDispose == 2) {
            int endX = Math.min(width, lastIx + lastIw);
            int endY = Math.min(height, lastIy + lastIh);
            for (int y = Math.max(0, lastIy); y < endY; y++) {
                int rowOffset = y * width;
                for (int x = Math.max(0, lastIx); x < endX; x++) {
                    masterCanvas[rowOffset + x] = 0;
                }
            }
        } else if (lastDispose == 3 && previousCanvas != null) {
            System.arraycopy(previousCanvas, 0, masterCanvas, 0, masterCanvas.length);
        }

        if (dispose == 3) {
            if (previousCanvas == null || previousCanvas.length != masterCanvas.length) {
                previousCanvas = new int[masterCanvas.length];
            }
            System.arraycopy(masterCanvas, 0, previousCanvas, 0, masterCanvas.length);
        }

        int i = 0;
        int line = 0;
        int inc = 8;
        int pass = 1;

        for (int y = 0; y < ih; y++) {
            int row = y;
            if (interlace) {
                if (line >= ih) {
                    pass++;
                    switch (pass) {
                        case 2: line = 4; break;
                        case 3: line = 2; inc = 4; break;
                        case 4: line = 1; inc = 2; break;
                    }
                }
                row = line;
                line += inc;
            }

            int targetY = iy + row;
            if (targetY >= 0 && targetY < height) {
                int rowOffset = targetY * width;
                for (int x = 0; x < iw; x++) {
                    int index = pixels[i++] & 0xff;
                    int targetX = ix + x;

                    if (targetX >= 0 && targetX < width) {
                        if (!transColor || index != transIndex) {
                            int color = act[index];
                            int r = (color >> 16) & 0xff;
                            int g = (color >> 8) & 0xff;
                            int b = color & 0xff;
                            int a = (color >> 24) & 0xff;

                            int rgba8888 = (r << 24) | (g << 16) | (b << 8) | a;
                            masterCanvas[rowOffset + targetX] = rgba8888;
                        }
                    }
                }
            } else {
                i += iw;
            }
        }

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        IntBuffer intBuffer = pixmap.getPixels().asIntBuffer();
        intBuffer.put(masterCanvas);
        pixmap.getPixels().rewind();

        lastDispose = dispose;
        lastIx = ix;
        lastIy = iy;
        lastIw = iw;
        lastIh = ih;

        dispose = 0;
        transColor = false;

        frames.addElement(new GifFrame(pixmap, gceDelay));
    }
}
