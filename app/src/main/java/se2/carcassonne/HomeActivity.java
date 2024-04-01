package se2.carcassonne;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import se2.carcassonne.databinding.HomeActivityBinding;
import se2.carcassonne.helper.resize.FullscreenHelper;
import se2.carcassonne.player.repository.PlayerRepository;
import se2.carcassonne.player.viewmodel.ChooseUsernameViewModel;

public class HomeActivity extends AppCompatActivity {
    HomeActivityBinding binding;
    private ChooseUsernameViewModel viewModel;
    PlayerRepository playerRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = HomeActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FullscreenHelper.setFullscreenAndImmersiveMode(this);
        playerRepository = new PlayerRepository();
        viewModel = new ChooseUsernameViewModel(playerRepository);
        showChooseUsernameDialog();
        viewModel.getMessageLiveData().observe(this, message -> binding.textView.setText(String.format(getString(R.string.welcome_homescreen), viewModel.getPlayerName(message))));
        binding.newGameBtn.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, GameLobbyActivity.class);
            startActivity(intent);
        });
    }

    private void showChooseUsernameDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ChooseUsernameDialogFragment dialogFragment = new ChooseUsernameDialogFragment(viewModel, playerRepository);
        dialogFragment.show(fragmentManager, "ChooseUsernameDialogFragment");
    }
}