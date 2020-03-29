package com.example.words;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class WordsFragement extends Fragment {

    private static final String VIEW_TYPE_SHP = "view_type_shp"; //sharedpreference 的文件名
    private static final String IS_USING_CARD_VIEW = "Is_using _card_view";

    private WordViewModel wordViewModel;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter1,myAdapter2;

    private FloatingActionButton floatingActionButton;

    private InputMethodManager inputMethodManager;

    private LiveData<List<Word>> filteredWords;
    public WordsFragement() {
        // Required empty public constructor
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // 显示搜索并且不会挤占原本的label
        inflater.inflate(R.menu.main,menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setMaxWidth(1000);
        // 在搜索框内开始输入，开始查找. 设置监听器
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                String pattern = s.trim(); //获取输入的字符串（去除了前后空格的）
                //filteredWords.removeObserver(requireActivity());  //要是前面添加了observer, 才需要这一句
                filteredWords = wordViewModel.findWordsWithPattern(pattern);
                filteredWords.observe(requireActivity(), new Observer<List<Word>>() {
                    @Override
                    public void onChanged(List<Word> words) {
                        int temp = myAdapter1.getItemCount();
                        myAdapter1.setAllWords(words);
                        myAdapter2.setAllWords(words);
                        if (temp != words.size()) {
                            myAdapter1.notifyDataSetChanged();
                            myAdapter2.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });
    }

    //点击了option里的item后的处理方法


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_data:
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setTitle("Clear all data?");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        wordViewModel.deleteAllWords();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create();
                builder.show();
                break;
            case R.id.switch_view:
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(VIEW_TYPE_SHP, Context.MODE_PRIVATE);
                boolean viewType = sharedPreferences.getBoolean(IS_USING_CARD_VIEW, false);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (viewType) {
                    recyclerView.setAdapter(myAdapter1);
                    editor.putBoolean(IS_USING_CARD_VIEW,false); //将卡片视图切换为正常视图后，也得把viewType切换为false，表明不再是card view
                }else{
                    recyclerView.setAdapter(myAdapter2);
                    editor.putBoolean(IS_USING_CARD_VIEW,true);
                }
                editor.apply();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_words_fragement, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        wordViewModel = new ViewModelProvider(requireActivity(),new SavedStateViewModelFactory(requireActivity().getApplication(),this)).get(WordViewModel.class);
        recyclerView = requireActivity().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        myAdapter1 = new MyAdapter(false,wordViewModel);
        myAdapter2 = new MyAdapter(true,wordViewModel);
        //recyclerView.setAdapter(myAdapter1); // 默认是适配器1
        //修改上面的默认，修改为：根据viewType来确定一开始进来是哪一种视图的适配器
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(VIEW_TYPE_SHP, Context.MODE_PRIVATE);
        boolean viewType = sharedPreferences.getBoolean(IS_USING_CARD_VIEW,false);
        if (viewType) {
            recyclerView.setAdapter(myAdapter2);
        }else {
            recyclerView.setAdapter(myAdapter1);
        }

        floatingActionButton = requireActivity().findViewById(R.id.floatingActionButton);
        filteredWords = wordViewModel.getAllWordsLive(); //初始化 filteredwords

        wordViewModel.getAllWordsLive().observe(requireActivity(), new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                int temp = myAdapter1.getItemCount();    //获取适配器有多少个子项
                myAdapter1.setAllWords(words);
                myAdapter2.setAllWords(words);
                if (temp != words.size()) {
                    myAdapter1.notifyDataSetChanged();
                    myAdapter2.notifyDataSetChanged();
                }
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_wordsFragement_to_addFragment);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(),0);
    }
}
