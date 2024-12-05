package com.yash.productivityapp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_FILE = 101;

    private TextView filePathTextView, uploadStatusTextView;
    private LinearLayout pdfContainerLayout;
    private EditText splitPageEditText;
    private Uri selectedPdfUri;  // To store selected PDF URI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filePathTextView = findViewById(R.id.filePathTextView);
        uploadStatusTextView = findViewById(R.id.uploadStatusTextView);
        pdfContainerLayout = findViewById(R.id.pdfContainerLayout);
        splitPageEditText = findViewById(R.id.splitPageEditText);

        Button uploadFileButton = findViewById(R.id.uploadFileButton);
        Button splitPdfButton = findViewById(R.id.splitPdfButton);

        // Open file picker when the upload button is clicked
        uploadFileButton.setOnClickListener(v -> openFilePicker());

        // When the "Split PDF" button is clicked, navigate to SplitPdfActivity
        splitPdfButton.setOnClickListener(v -> {
            String splitPageInput = splitPageEditText.getText().toString();
            if (TextUtils.isEmpty(splitPageInput)) {
                Toast.makeText(this, "Please enter a page number to split", Toast.LENGTH_SHORT).show();
                return;
            }

            int splitPage = Integer.parseInt(splitPageInput);

            if (splitPage <= 0) {
                Toast.makeText(this, "Please enter a valid split page number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ensure that the selected PDF URI is not null
            if (selectedPdfUri == null) {
                Toast.makeText(this, "No PDF file selected", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pass the selected PDF URI and splitPage to SplitPdfActivity
            Intent intent = new Intent(MainActivity.this, SplitPdfActivity.class);
            intent.putExtra("pdfUri", selectedPdfUri.toString()); // Pass URI as string
            intent.putExtra("splitPage", splitPage); // Pass split page
            startActivity(intent);
        });
    }

    private void openFilePicker() {
        // Open file picker using Storage Access Framework (SAF)
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");  // Restrict file picker to PDFs
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK && data != null) {
            selectedPdfUri = data.getData();  // Get the selected file URI
            if (selectedPdfUri != null) {
                filePathTextView.setText("File Selected: " + selectedPdfUri.toString());
                renderPdf(selectedPdfUri);
            }
        }
    }

    private void renderPdf(Uri fileUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                // Open the PDF file
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(fileUri, "r");
                if (parcelFileDescriptor != null) {
                    PdfRenderer pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                    int pageCount = pdfRenderer.getPageCount();

                    // Loop through all pages and render them
                    for (int i = 0; i < pageCount; i++) {
                        PdfRenderer.Page page = pdfRenderer.openPage(i);

                        // Create a Bitmap to render the page to
                        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                        // Create an ImageView to hold the rendered page
                        ImageView pageImageView = new ImageView(this);
                        pageImageView.setImageBitmap(bitmap);

                        // Add the ImageView to the container (which is scrollable)
                        pdfContainerLayout.addView(pageImageView);

                        // Close the page after rendering
                        page.close();
                    }

                    pdfRenderer.close();
                    uploadStatusTextView.setText("PDF rendered successfully");
                }
            } catch (IOException e) {
                e.printStackTrace();
                uploadStatusTextView.setText("Error rendering PDF: " + e.getMessage());
            }
        }
    }
}
