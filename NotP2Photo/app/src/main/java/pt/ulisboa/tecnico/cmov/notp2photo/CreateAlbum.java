package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class CreateAlbum extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_album);
    }

    public void createAlbum(View v){
        EditText albumNameText = findViewById(R.id.editAlbumName);
        String albumName = albumNameText.getText().toString();

        Intent intent = new Intent(this, UploadFolderService.class);
        intent.putExtra("album", albumName);
        startService(intent);

        finish();
    }


}
