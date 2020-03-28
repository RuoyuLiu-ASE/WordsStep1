package com.example.words;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddFragment extends Fragment {

    private Button buttonSubmit;
    private EditText editTextEnglish,editTextChinese;
    private WordViewModel wordViewModel;

    public AddFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = requireActivity();
        buttonSubmit = activity.findViewById(R.id.submit_button);
        editTextChinese = activity.findViewById(R.id.editTextChinese);
        editTextEnglish = activity.findViewById(R.id.editTextEnglish);

        editTextEnglish.requestFocus(); //用来获取焦点，即：光标直接就跳到editEnglish, 准备开始输入

        buttonSubmit.setEnabled(false);     //一开始的时候，按键应该是灰色的
        final InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editTextEnglish,0);

        wordViewModel = new ViewModelProvider(requireActivity(), new SavedStateViewModelFactory(requireActivity().getApplication(),this)).get(WordViewModel.class);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String english = editTextEnglish.getText().toString().trim();
                String chinese = editTextChinese.getText().toString().trim(); //去除字符串的前后空格
                buttonSubmit.setEnabled(!english.isEmpty() && !chinese.isEmpty()); //当两个字符串都不为空时才能按下按钮
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        editTextEnglish.addTextChangedListener(textWatcher);
        editTextChinese.addTextChangedListener(textWatcher);

        buttonSubmit.setOnClickListener(new View.OnClickListener() { //点击提交键之后
            @Override
            public void onClick(View view) {
                String english = editTextEnglish.getText().toString().trim();
                String chinese = editTextChinese.getText().toString().trim();
                Word word = new Word(english,chinese);
                wordViewModel.insertWords(word); // 把新添加的单词加入ViewModel里

                NavController navController = Navigation.findNavController(view);//导航回初始界面
                navController.navigateUp();

                //把键盘隐藏起来
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
            }
        });

    }
}
