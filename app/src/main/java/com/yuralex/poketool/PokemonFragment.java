package com.yuralex.poketool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.omkarmoghe.pokemap.controllers.app_preferences.PokemapSharedPreferences;
import com.omkarmoghe.pokemap.controllers.net.NianticManager;
import com.omkarmoghe.pokemap.views.LoginActivity;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PokemonFragment extends Fragment implements MainActivity.Updatable {
    private static final String TAG = PokemonFragment.class.getSimpleName();

    private static final int SORT_CP = 0;
    private static final int SORT_IV = 1;
    private static final int SORT_TYPE_CP = 2;
    private static final int SORT_TYPE_IV = 3;
    private static final int SORT_RECENT = 4;
    private static final String STATE_POKEMONS = "pokemons";

    private SparseArray<PokemonImg> mPokemonImages;
    private PokemonGo mGo;
    private ArrayList<PokemonDto> mPokemons;
    private int mSort;

    private FragmentActivity mActivity;
    private StableArrayAdapter mGridAdapter;
    private GridView mGridView;
    private PokemapSharedPreferences mPref;

    public PokemonFragment() {
    }

    public static PokemonFragment newInstance() {
        return new PokemonFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        if (mActivity == null)
            return;

        mPref = new PokemapSharedPreferences(mActivity);

        setHasOptionsMenu(true);

        DaoPokemon daoPokemon = new DaoPokemon();
        mPokemonImages = daoPokemon.getAllPokemon();
        NianticManager nianticManager = NianticManager.getInstance();
        mGo = nianticManager.getPokemonGo();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(STATE_POKEMONS, mPokemons);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pokemon, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.gridViewPokemon);
        //mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        //mGridView.setMultiChoiceModeListener(new MultiChoiceModeListener());

        mSort = mPref.getSortType();
        Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);
        if (spinner != null) {
            spinner.setSelection(mSort);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mPref.setSortType(position);
                    mSort = position;
                    sortPokemon();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        if (mPokemons == null && savedInstanceState != null)
            mPokemons = savedInstanceState.getParcelableArrayList(STATE_POKEMONS);

        if (mPokemons == null)
            loadPokemons();

        updateList();
        return rootView;
    }

    private void sortPokemon() {
        Collections.sort(mPokemons, getPokemonComparator());
        mGridAdapter.notifyDataSetChanged();
        mGridView.invalidateViews();
    }

    private static float pokemonIv(PokemonDto p) {
        return (p.getIndividualAttack()
                + p.getIndividualDefense()
                + p.getIndividualStamina()) * 100 / 45f;
    }

    private static class ComparatorIv implements Comparator<PokemonDto> {
        public int compare(PokemonDto p1, PokemonDto p2) {
            return (int) (pokemonIv(p2) - pokemonIv(p1));
        }
    }

    private static class ComparatorCp implements Comparator<PokemonDto>{
        public int compare(PokemonDto p1, PokemonDto p2) {
            return p2.getCp() - p1.getCp();
        }
    }

    private static class ComparatorTypeIv implements Comparator<PokemonDto>{
        public int compare(PokemonDto p1, PokemonDto p2) {
            int compare = p1.getPokemonId() - p2.getPokemonId();
            return compare != 0 ? compare : (int) (pokemonIv(p2) - pokemonIv(p1));
        }
    }

    private static class ComparatorTypeCp implements Comparator<PokemonDto>{
        public int compare(PokemonDto p1, PokemonDto p2) {
            int compare = p1.getPokemonId() - p2.getPokemonId();
            return compare != 0 ? compare : p2.getCp() - p1.getCp();
        }
    }

    private class ComparatorRecent implements Comparator<PokemonDto> {
        public int compare(PokemonDto p1, PokemonDto p2) {
            return (int) (p2.getCreationTimeMs() - p1.getCreationTimeMs());
        }
    }

    private Comparator<PokemonDto> getPokemonComparator() {
        Comparator<PokemonDto> comparator;
        switch (mSort) {
            case SORT_CP:
                comparator = new ComparatorCp();
                break;
            case SORT_IV:
                comparator = new ComparatorIv();
                break;
            case SORT_TYPE_CP:
                comparator = new ComparatorTypeCp();
                break;
            case SORT_TYPE_IV:
                comparator = new ComparatorTypeIv();
                break;
            case SORT_RECENT:
                comparator = new ComparatorRecent();
                break;
            default:
                comparator = new ComparatorCp();
        }
        return comparator;
    }

    public void update() {
        loadPokemons();
        updateList();
    }

    private void loadPokemons() {
        try {
            if (mGo != null) {
                mPokemons = new ArrayList<>();
                List<Pokemon> pokemons = mGo.getInventories().getPokebank().getPokemons();
                for (Pokemon p: pokemons)
                    mPokemons.add(new PokemonDto(p));
            }
        } catch (LoginFailedException | RemoteServerException e) {
            e.printStackTrace();
        }
    }

    private void updateList() {
        if (mPokemons == null) {
            startActivity(new Intent(mActivity, LoginActivity.class));
            mActivity.finish();
            return;
        }

        Collections.sort(mPokemons, getPokemonComparator());

        mGridAdapter = new StableArrayAdapter(mActivity,
                android.R.layout.simple_list_item_1, mPokemons);
        if (mGridView != null) {
            mGridView.setAdapter(mGridAdapter);
        } else {
            Log.e(TAG, "gridView == null");
        }
    }

    private class StableArrayAdapter extends ArrayAdapter<PokemonDto> {
        private List<PokemonDto> pokemons;
        private HashMap<Integer, Boolean> selection = new HashMap<>();

        StableArrayAdapter(Context context, int textViewResourceId, List<PokemonDto> pokemons) {
            super(context, textViewResourceId, pokemons);
            this.pokemons = pokemons;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder")
            View rowView = inflater.inflate(R.layout.pokemon_list_item, parent, false);
            TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
            TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
            TextView thirdLine = (TextView) rowView.findViewById(R.id.thirdLine);
            TextView fourthLine = (TextView) rowView.findViewById(R.id.fourthLine);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            PokemonDto p = pokemons.get(position);
            if (p != null) {
                String name = p.getNickname();
                if ("".equals(name) || name == null) {
                    name = properCase(p.getPokemonName());
                }
                firstLine.setText(name);
                secondLine.setText(String.format(Locale.ROOT, "IV%.2f%%", p.getIvRatio() * 100f));
                thirdLine.setText(String.format(Locale.ROOT, "CP%d lv%.1f", p.getCp(), p.getLevel()));
                fourthLine.setText(String.format(Locale.ROOT, "%d/%d/%d",
                        p.getIndividualAttack(), p.getIndividualDefense(), p.getIndividualStamina()));
                imageView.setImageResource(mPokemonImages.get(p.getPokemonId()).getImagem());
            }

            rowView.setBackgroundColor(Color.TRANSPARENT); //default color
            if (selection.get(position) != null) {
                rowView.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.holo_blue_light));// this is a selected position so make it red
            }
            return rowView;
        }

        String properCase (String inputVal) {
            // Empty strings should be returned as-is.

            if (inputVal.length() == 0) return "";

            // Strings with only one character uppercased.

            if (inputVal.length() == 1) return inputVal.toUpperCase();

            // Otherwise uppercase first letter, lowercase the rest.

            return inputVal.substring(0,1).toUpperCase()
                    + inputVal.substring(1).toLowerCase();
        }
    }
}
