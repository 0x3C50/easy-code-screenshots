package me.x150.intellijcodescreenshots.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.util.TextRange;
import com.intellij.ui.JBColor;
import com.intellij.ui.scale.JBUIScale;
import me.x150.intellijcodescreenshots.OptionsServiceProvider;
import org.apache.commons.imaging.common.ImageBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.regex.Pattern;

import static com.intellij.codeInsight.hint.EditorFragmentComponent.getBackgroundColor;

// Creates a screenshot of code
// Partially lifted from https://github.com/amaembo/screenshoter
public class ScreenshotBuilder {
	static final JBColor green = new JBColor(new Color(43, 204, 31), new Color(43, 204, 31));
	static final JBColor yellow = new JBColor(new Color(204, 159, 31), new Color(204, 159, 31));
	static final JBColor red = new JBColor(new Color(204, 50, 31), new Color(204, 50, 31));
	@NotNull
	private final Editor editor;

	public ScreenshotBuilder(@NotNull Editor editor) {
		this.editor = editor;
	}

	private static TextRange getRange(Editor editor) {
		SelectionModel selectionModel = editor.getSelectionModel();
		int start = selectionModel.getSelectionStart();
		int end = selectionModel.getSelectionEnd();
		return new TextRange(start, end);
	}

	private static void includePoint(Rectangle2D r, Point2D p) {
		if (r.isEmpty()) {
			r.setFrame(p, new Dimension(1, 1));
		} else {
			r.add(p);
		}
	}

	@Nullable
	public BufferedImage createImage() {
		TextRange range = getRange(editor);

		Document document = editor.getDocument();
		EditorState state = EditorState.from(editor);
		try {
			resetEditor();
			OptionsServiceProvider.State options = OptionsServiceProvider.getInstance().getState();
			double scale = options.scale;
			//System.out.println(scale);
			JComponent contentComponent = editor.getContentComponent();
			Graphics2D contentGraphics = (Graphics2D) contentComponent.getGraphics();
			AffineTransform currentTransform = contentGraphics.getTransform();
			AffineTransform newTransform = new AffineTransform(currentTransform);
			newTransform.scale(scale, scale);
			// To flush glyph cache
			paint(contentComponent, newTransform, 1, 1, JBColor.BLACK, options, 0);
			String text = document.getText(range);
			Rectangle2D r = getSelectionRectangle(range, text, options);

			newTransform.translate(-r.getX(), -r.getY());

			return paint(contentComponent,
					newTransform,
					(int) (r.getWidth() * scale),
					(int) (r.getHeight() * scale),
					getBackgroundColor(editor, false),
					options,
					(int) (options.innerPadding * scale));
		} catch (Exception e) {
			Logger.getInstance(ImageBuilder.class).error(e);
			return null;
		} finally {
			state.restore(editor);
		}
	}

	BufferedImage paint(JComponent contentComponent, AffineTransform at, int width, int height, Color backgroundColor, OptionsServiceProvider.State state, int innerPadding) {
		int outerPaddingHoriMapped = (int) (state.outerPaddingHoriz * state.scale);
		int outerPaddingVertMapped = (int) (state.outerPaddingVert * state.scale);
		double indicatorDimensions = 10 * state.scale;
		double windowControlsPadding = 6 * state.scale;
		double scale = JBUIScale.sysScale(contentComponent);
		int paddingX = innerPadding + outerPaddingHoriMapped;
		int paddingY = innerPadding + outerPaddingVertMapped;

		double windowControlsHeightWithPadding = windowControlsPadding + indicatorDimensions + windowControlsPadding;
		double preferredPaddingTopWithIndicators = Math.max(windowControlsHeightWithPadding, innerPadding);

		@SuppressWarnings("UndesirableClassUsage") BufferedImage img = new BufferedImage((int) (outerPaddingHoriMapped + innerPadding + width * scale + innerPadding + outerPaddingHoriMapped),
				(int) (outerPaddingVertMapped + (state.showWindowControls ? preferredPaddingTopWithIndicators : innerPadding) + height * scale + innerPadding + outerPaddingVertMapped),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		int scaledWidth = (int) (width * scale);
		int scaledHeight = (int) (height * scale);
		int imgWidth = img.getWidth();
		int imgHeight = img.getHeight();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setPaint(state.getBackgroundColor());
		g.fillRect(0, 0, imgWidth, imgHeight);
		g.setPaint(backgroundColor);
		int windowRoundness = (int) (state.windowRoundness * state.scale);
		g.fillRoundRect(outerPaddingHoriMapped,
				outerPaddingVertMapped,
				imgWidth - outerPaddingHoriMapped * 2,
				imgHeight - outerPaddingVertMapped * 2,
				windowRoundness,
				windowRoundness);
		double xOffset = 0;
		if (state.showWindowControls) {
			for (JBColor jbColor : new JBColor[]{red, yellow, green}) {
				g.setPaint(jbColor);
				g.fillOval((int) (outerPaddingHoriMapped + windowControlsPadding + xOffset),
						(int) (outerPaddingVertMapped + windowControlsPadding),
						(int) indicatorDimensions,
						(int) indicatorDimensions);
				xOffset += indicatorDimensions + windowControlsPadding;
			}
		}
		g.translate(paddingX, state.showWindowControls ? outerPaddingVertMapped + preferredPaddingTopWithIndicators : paddingY);
		g.clipRect(0, 0, scaledWidth, scaledHeight);

		g.transform(at);
		contentComponent.paint(g);
		return img;
	}

	private void resetEditor() {
		Document document = editor.getDocument();
		TextRange range = getRange(editor);
		editor.getSelectionModel().setSelection(0, 0);

		editor.getCaretModel().moveToOffset(range.getStartOffset() == 0 ? document.getLineEndOffset(document.getLineCount() - 1) : 0);
		if (editor instanceof EditorEx) {
			((EditorEx) editor).setCaretEnabled(false);
		}
		editor.getSettings().setCaretRowShown(false);
	}

//    long getSelectedSize() {
//        OptionsServiceProvider.State options = OptionsServiceProvider.getInstance(project).getState();
//        Rectangle2D rectangle = getSelectionRectangle();
//        double sizeX = rectangle.getWidth() + options.innerPadding * 2;
//        double sizeY = rectangle.getHeight() + options.innerPadding * 2;
//        return (long) (sizeX * sizeY * options.scale * options.scale);
//    }

//    @NotNull
//    private Rectangle2D getSelectionRectangle() {
//        OptionsServiceProvider.State options = OptionsServiceProvider.getInstance(project).getState();
//        TextRange range = getRange(editor);
//        Document document = editor.getDocument();
//        String text = document.getText(range);
//        return getSelectionRectangle(range, text, options);
//    }

	@NotNull
	private Rectangle2D getSelectionRectangle(TextRange range, String text, OptionsServiceProvider.State options) {
		int start = range.getStartOffset();
		int end = range.getEndOffset();
		Rectangle2D r = new Rectangle2D.Double();

		for (int i = start; i < end; i++) {
			if (options.removeIndentation && Character.isWhitespace(text.charAt(i-start))) {
				// if this character is a whitespace and we're removing indentation, ignore it
				// -> don't add the position it's in to the rect we screenshot
				// the rect will be expanded if something else expands it after this whitespace
				continue;
			}
			// offsetToXY gives us the starting position of the character at i
			// -> the top left coordinate of the character
			// to add the character fully, we need to make sure the top right / bottom right point is also in the range
			// -> get the position of the top left of the *next* character WITHOUT soft wrapping to get the top left position of the theoretical subsequent character

			/*
			Position of character 0
			│
			│             │Position of character 0+1
			▼             ▼
			┌───────────┐ ┌─────┐
			│     xx    │ │xxx  │
			│    xx x   │ │x xx │
			│    x   xx │ │x  x │
			│  xxxxxxxx │ │xxxxx│
			│ xx      x │ │x   x│
			│x        xx│ │xxxxx│
			└───────────┘ └─────┘
						 ▲
						 │
						 Gap for illustrative purposes. Does not exist

			│             │
			└─────────────┘
			Full range of character 0
			Both points (and thus this range) added to rect
			 */

			Point point = editor.offsetToXY(i, false, false);
			includePoint(r, point);
			includePoint(r, new Point2D.Double(point.getX(), point.getY() + editor.getLineHeight()));
			point = editor.offsetToXY(i+1, false, true);
			includePoint(r, point);
			includePoint(r, new Point2D.Double(point.getX(), point.getY() + editor.getLineHeight()));
		}
		for (Inlay<?> inlay : editor.getInlayModel().getInlineElementsInRange(start, end)) {
			Rectangle bounds = inlay.getBounds();
			if (bounds != null) {
				r.add(bounds);
			}
		}
		return r;
	}

	public static class EditorState {
		private final TextRange range;
		private final int offset;
		private final boolean caretRow;

		EditorState(TextRange range, int offset, boolean caretRow) {
			this.range = range;
			this.offset = offset;
			this.caretRow = caretRow;
		}

		@NotNull
		static EditorState from(Editor editor) {
			TextRange range = getRange(editor);
			int offset = editor.getCaretModel().getOffset();

			return new EditorState(range, offset, editor.getSettings().isCaretRowShown());
		}

		void restore(Editor editor) {
			editor.getSettings().setCaretRowShown(caretRow);
			SelectionModel selectionModel = editor.getSelectionModel();
			CaretModel caretModel = editor.getCaretModel();

			if (editor instanceof EditorEx) {
				((EditorEx) editor).setCaretEnabled(true);
			}
			caretModel.moveToOffset(offset);

			selectionModel.setSelection(range.getStartOffset(), range.getEndOffset());
		}
	}
}
