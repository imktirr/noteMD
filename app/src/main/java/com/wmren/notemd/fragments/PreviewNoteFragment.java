package com.wmren.notemd.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.wmren.notemd.R;
import com.wmren.notemd.tools.MarkdownParser;


public class PreviewNoteFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String NOTE_TITLE = "param1";
    public static final String NOTE_CONTENT = "param2";

    private String noteTitle;
    private String noteContent;
    private String receive;
    private String html;
    private String output;

    public PreviewNoteFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            noteTitle = getArguments().getString(NOTE_TITLE);
            noteContent = getArguments().getString(NOTE_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_preview_note, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            noteTitle = getArguments().getString(NOTE_TITLE);
            noteContent = getArguments().getString(NOTE_CONTENT);
        }
        View rootView = getView();
        WebView webView = rootView.findViewById(R.id.noteView);
        //markdown逻辑部分，调用noteTitle与noteContent
        noteTitle = "# " + noteTitle + "\n";
        receive = noteTitle + noteContent;
        html = MarkdownParser.parse(receive);
        output = MarkdownParser.configHtml(html);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBlockNetworkImage(false);

        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.loadDataWithBaseURL(null, output, "text/html", "utf-8", null);
    }

}
