package com.jonys.appdesigner.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.jonys.appdesigner.R;
import com.jonys.appdesigner.databinding.ActivityXmlPreviewBinding;

import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.SymbolInputView;

import org.eclipse.tm4e.core.registry.IGrammarSource;
import org.eclipse.tm4e.core.registry.IThemeSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class XmlPreviewActivity extends BaseActivity {

    public static final String EXTRA_KEY_XML = "xml";
    private SharedPreferences prefs;

    private ActivityXmlPreviewBinding binding;
    private CodeEditor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        binding = ActivityXmlPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        editor = binding.editor;

        binding.symbolInput.bindEditor(editor);
        setupSymbols(binding.symbolInput);
        try {
            setEditorTmTheme(
                    getAssets().open("textmate/color_schemes/GitHub.tmTheme"), "GitHub.tmTheme");
            var language =
                    TextMateLanguage.create(
                            IGrammarSource.fromInputStream(
                                    getAssets().open("textmate/xml/syntaxes/xml.tmLanguage.json"),
                                    "xml.tmLanguage.json",
                                    null),
                            new InputStreamReader(
                                    getAssets().open("textmate/xml/language-configuration.json")),
                            ((TextMateColorScheme) editor.getColorScheme()).getThemeSource());

            editor.setEditorLanguage(language);
        } catch (IOException e) {
            e.printStackTrace();
        }

        editor.setText(getIntent().getStringExtra(EXTRA_KEY_XML));
        editor.setTypefaceText(jetBrainsMono());
        editor.setTypefaceLineNumber(jetBrainsMono());
        editor.setTextSizePx(prefs.getFloat("editor_font_size", 18.0f));

        binding.btnOptions.setOnClickListener(
                v -> {
                    final PopupMenu popupMenu = new PopupMenu(this, v);
                    popupMenu.inflate(R.menu.xml_preview_menu);

                    popupMenu.setOnMenuItemClickListener(
                            new PopupMenu.OnMenuItemClickListener() {

                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.menu_copy:
                                            {
                                                ClipboardManager clip =
                                                        (ClipboardManager)
                                                                getSystemService(CLIPBOARD_SERVICE);
                                                clip.setPrimaryClip(
                                                        ClipData.newPlainText(
                                                                "clipboard",
                                                                getIntent()
                                                                        .getStringExtra(
                                                                                EXTRA_KEY_XML)));
                                                Snackbar.make(
                                                                binding.getRoot(),
                                                                "Copied",
                                                                Snackbar.LENGTH_SHORT)
                                                        .setAnchorView(binding.symbolInput)
                                                        .show();
                                                return true;
                                            }

                                        case R.id.menu_save:
                                            {
                                                return true;
                                            }

                                        default:
                                            return false;
                                    }
                                }
                            });
                    if (popupMenu.getMenu() instanceof MenuBuilder) {

                        MenuPopupHelper helper =
                                new MenuPopupHelper(this, (MenuBuilder) popupMenu.getMenu(), v);
                        helper.setForceShowIcon(true);
                        helper.show();
                    } else {
                        popupMenu.show();
                    }
                });

        binding.btnBack.setOnClickListener(
                v -> {
                    prefs.edit().putFloat("editor_font_size", editor.getTextSizePx()).commit();
                    super.onBackPressed();
                });
    }

    private void setEditorTmTheme(InputStream is, String fileName) {
        try {
            var colorScheme = editor.getColorScheme();
            IThemeSource themeSource;
            if (!(colorScheme instanceof TextMateColorScheme)) {
                themeSource = IThemeSource.fromInputStream(is, fileName, null);

                colorScheme = TextMateColorScheme.create(themeSource);
            }

            editor.setColorScheme(colorScheme);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSymbols(SymbolInputView symbolInput) {
        symbolInput.addSymbols(
                new String[] {
                    "➜", "<", ">", "/", "=", "\"", ":", "@", "+", "(", ")", ";", ",", ".", "?", "|",
                    "\\", "&", "[", "]", "{", "}", "_", "-"
                },
                new String[] {
                    "\t", "<>", ">", "/", "=", "\"\"", ":", "@", "+", "()", ")", ";", ",", ".", "?",
                    "|", "\\", "&", "[]", "]", "{}", "}", "_", "-"
                });
        symbolInput.forEachButton(
                (b) -> {
                    b.setTypeface(jetBrainsMono());
                });
    }

    private Typeface jetBrainsMono() {
        return Typeface.createFromAsset(getAssets(), "JetBrainsMono-Regular.ttf");
    }

    @Override
    public void onBackPressed() {
        prefs.edit().putFloat("editor_font_size", editor.getTextSizePx()).commit();
        super.onBackPressed();
    }
}
