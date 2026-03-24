package com.vomiter.survivorsdelight.data.book.builder;

import java.util.Objects;

public class TextBuilder {
    private final StringBuilder sb = new StringBuilder();

    /* ---------- Factory ---------- */
    public static TextBuilder create() { return new TextBuilder(); }
    public static TextBuilder of(String initial) { return new TextBuilder().text(initial); }

    /* ---------- Core ---------- */
    public TextBuilder text(String s) { sb.append(Objects.requireNonNullElse(s, "")); return this; }
    public TextBuilder raw(String s)  { sb.append(Objects.requireNonNullElse(s, "")); return this; }
    public TextBuilder space()        { sb.append(' '); return this; }

    /** 基本 append（字串版），可鏈式 */
    public TextBuilder append(String s) {
        sb.append(Objects.requireNonNullElse(s, ""));
        return this;
    }

    /** 基本 append（另一個 TextBuilder 版），可鏈式 */
    public TextBuilder append(TextBuilder other) {
        if (other != null) sb.append(other.sb);
        return this;
    }

    /** 以空白連接到目前內容（字串版），可鏈式 */
    public TextBuilder appendWithSpace(String s) {
        if (s == null || s.isEmpty()) return this;
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') sb.append(' ');
        sb.append(s);
        return this;
    }

    /** 以空白連接到目前內容（TextBuilder 版），可鏈式 */
    public TextBuilder appendWithSpace(TextBuilder other) {
        if (other == null) return this;
        if (other.sb.length() == 0) return this;
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') sb.append(' ');
        sb.append(other.sb);
        return this;
    }

    /** 在目前內容後追加 a + " " + b（字串版），可鏈式 */
    public TextBuilder appendWithSpace(String a, String b) {
        if (a != null && !a.isEmpty()) sb.append(a);
        if ((a != null && !a.isEmpty()) && (b != null && !b.isEmpty())) sb.append(' ');
        if (b != null && !b.isEmpty()) sb.append(b);
        return this;
    }

    /** 在目前內容後追加 a + " " + b（TextBuilder 版），可鏈式 */
    public TextBuilder appendWithSpace(TextBuilder a, TextBuilder b) {
        String sa = (a == null) ? "" : a.toString();
        String sb2 = (b == null) ? "" : b.toString();
        return appendWithSpace(sa, sb2);
    }

    /** 多段以單一空白串起並追加到目前內容：忽略 null/空字串 */
    public TextBuilder joinWithSpace(Object... parts) {
        boolean first = true;
        for (Object p : parts) {
            String s = String.valueOf(p);
            if (s == null || s.isEmpty() || "null".equals(s)) continue;
            if (!first) sb.append(' ');
            sb.append(s);
            first = false;
        }
        return this;
    }

    @Override public String toString() { return sb.toString(); }

    /* ---------- Patchouli 標記工具 ---------- */
    private static String wrap(String tag, String content) { return "$(" + tag + ")" + content + "$()"; }

    /* 換行 / 段落 / 列表 */
    public TextBuilder br() { sb.append("$(br)"); return this; }
    public TextBuilder p()  { sb.append("$(p)");  return this; }
    public TextBuilder li(String content) { sb.append("$(li)").append(content); return this; }

    /* 標題 / 強調 */
    public TextBuilder header(String content) { sb.append("$(header)").append(content); return this; }
    public TextBuilder bold(String content)   { sb.append(wrap("bold", content)); return this; }
    public TextBuilder italic(String content) { sb.append(wrap("italic", content)); return this; }
    public TextBuilder strike(String content) { sb.append(wrap("strike", content)); return this; }
    public TextBuilder obf(String content)    { sb.append(wrap("obf", content)); return this; }
    public TextBuilder thing(String content)  { sb.append(wrap("thing", content)); return this; }

    /* 連結（內部/外部） */
    public TextBuilder link(String target, String text) {
        sb.append("$(").append("l:").append(target).append(")").append(text).append("$()");
        return this;
    }
    public TextBuilder url(String url, String text) {
        sb.append("$(").append("l:").append(url).append(")").append(text).append("$()");
        return this;
    }

    /* Keybind */
    public TextBuilder key(String keybindId) {
        sb.append("$(").append("k:").append(keybindId).append(")");
        return this;
    }

    /* 顏色 */
    public enum McColor {
        BLACK("0"), DARK_BLUE("1"), DARK_GREEN("2"), DARK_AQUA("3"),
        DARK_RED("4"), DARK_PURPLE("5"), GOLD("6"), GRAY("7"),
        DARK_GRAY("8"), BLUE("9"), GREEN("a"), AQUA("b"),
        RED("c"), LIGHT_PURPLE("d"), YELLOW("e"), WHITE("f");
        final String code; McColor(String code) { this.code = code; } public String code() { return code; }
    }
    public TextBuilder color(McColor color, String content) {
        sb.append("$(").append(color.code).append(")").append(content).append("$()");
        return this;
    }
    public TextBuilder hex(String hexWithoutHash, String content) {
        sb.append("$(").append("#").append(hexWithoutHash).append(")").append(content).append("$()");
        return this;
    }
    public TextBuilder reset() { sb.append("$()"); return this; }

    /* 常用片段 */
    public TextBuilder titledLine(String title, String body) {
        sb.append(wrap("bold", title)).append(' ').append(body);
        return this;
    }
    public TextBuilder colon() { sb.append(": "); return this; }
    public TextBuilder dash()  { sb.append(" - "); return this; }
    public TextBuilder dot()   { sb.append("."); return this; }

    /* 自訂標籤（擴充用） */
    public TextBuilder customTag(String tag, String content) {
        sb.append(wrap(tag, content));
        return this;
    }

    public TextBuilder item(String content){
        sb.append(wrap("item", content));
        return this;
    }

}
