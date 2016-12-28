package com.yuralex.poketool;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.omkarmoghe.pokemap.controllers.net.NianticManager;
import com.omkarmoghe.pokemap.views.LoginActivity;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.CaptchaActiveException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.Locale;

public class PokedexFragment extends Fragment implements MainActivity.Updatable {
    private static final String TAG = PokedexFragment.class.getSimpleName();
    private static final String STATE_POKEDEX = "pokedex";

    private SparseArray<PokemonImg> mPokemonImages;
    private FragmentActivity mActivity;
    private StableArrayAdapter mGridAdapter;
    private GridView mGridView;
    private PokemonGo mGo;
    private PokedexDto pokedex;

    public PokedexFragment() {
        // Required empty public constructor
    }

    public static PokedexFragment newInstance() {
        return new PokedexFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        if (mActivity == null)
            return;

        DaoPokemon daoPokemon = new DaoPokemon();
        mPokemonImages = daoPokemon.getAllPokemon();

        NianticManager nianticManager = NianticManager.getInstance();
        mGo = nianticManager.getPokemonGo();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(STATE_POKEDEX, pokedex);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pokedex, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.gridViewPokedex);

        if (pokedex == null && savedInstanceState != null)
            pokedex = savedInstanceState.getParcelable(STATE_POKEDEX);

        if (pokedex == null)
            loadPokedex();

        updateList();
        return rootView;
    }

    private void updateList() {
        if (pokedex == null) {
            startActivity(new Intent(mActivity, LoginActivity.class));
            mActivity.finish();
            return;
        }

        mGridAdapter = new StableArrayAdapter(mActivity, android.R.layout.simple_list_item_1, pokedex, mPokemonImages);
        if (mGridView != null) {
            mGridView.setAdapter(mGridAdapter);
        } else {
            Log.e(TAG, "gridView == null");
        }
    }

    private void loadPokedex() {
        if (mGo != null) {
            Inventories inventories = mGo.getInventories();
            pokedex = new PokedexDto(inventories.getPokebank(), inventories.getCandyjar());
        }
    }

    @Override
    public void update() {
        loadPokedex();
        updateList();
    }

    private class StableArrayAdapter extends ArrayAdapter<Pokemon> {
        private final PokedexDto pokedex;
        private final SparseArray<PokemonImg> images;

        StableArrayAdapter(Context context, int textViewResourceId, PokedexDto pokedex, SparseArray<PokemonImg> images) {
            super(context, textViewResourceId);
            this.pokedex = pokedex;
            this.images = images;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View rowView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.pokemon_list_item, parent, false);

            } else {
                rowView = convertView;
            }

            TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
            TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
            TextView thirdLine = (TextView) rowView.findViewById(R.id.thirdLine);
            TextView fourthLine = (TextView) rowView.findViewById(R.id.fourthLine);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

            int pokedexId = position + 1;
            PokedexEntryDto entry = pokedex.get(pokedexId);

            if (entry != null) {

                firstLine.setText(entry.getName());
                secondLine.setText(String.format(Locale.ROOT, "pokemons %d", entry.getPokemonCount()));
                if (entry.getPokemonCount() == 0) {
                    firstLine.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
                    secondLine.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
                } else {
                    firstLine.setTextColor(ContextCompat.getColor(getContext(), android.R.color.tertiary_text_light));
                    secondLine.setTextColor(ContextCompat.getColor(getContext(), android.R.color.tertiary_text_light));
                }
                if (entry.getCandy() < 0)
                    thirdLine.setText("candies ?/?");
                else if (entry.getCandyToEvolve() < 0)
                    thirdLine.setText(String.format(Locale.ROOT, "candies %d/?", entry.getCandy()));
                else if (entry.getCandyToEvolve() == 0)
                    thirdLine.setText(String.format(Locale.ROOT, "candies %d/-", entry.getCandy()));
                else
                    thirdLine.setText(String.format(Locale.ROOT, "candies %d/%d", entry.getCandy(), entry.getCandyToEvolve()));
                if (entry.getCandy() >= entry.getCandyToEvolve() && entry.getCandyToEvolve() > 0) {
                    thirdLine.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_green_dark));
                } else {
                    thirdLine.setTextColor(ContextCompat.getColor(getContext(), android.R.color.tertiary_text_light));
                }
                fourthLine.setText(String.format(Locale.ROOT, "E %d P %d", entry.getEvolutions() + entry.getEvolutionsExtra(), entry.getPokemonForGrinder()));
                PokemonImg pokeImg = images.get(pokedexId);
                if (pokeImg != null) {
                    imageView.setImageResource(images.get(pokedexId).getImageResourceId());
                }
            } else {
                secondLine.setText("");
                thirdLine.setText("");
                fourthLine.setText("");
                imageView.setImageResource(0);
            }

            return rowView;
        }

        @Override
        public int getCount() {
            return pokedex.count();
        }
    }
}
