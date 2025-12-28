package com.carlos.grabredenvelope.fragment

import android.content.Intent

interface IMainFragment {
     fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}