package com.example.storesroomsqlite

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.storesroomsqlite.databinding.FragmentEditStoreBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class EditStoreFragment : Fragment() {

    private var _binding: FragmentEditStoreBinding? = null
    private val binding get() = _binding!!

    private var mActivity: MainActivity? = null
    private var mIsEditMode: Boolean = false
    private var mStoreEntity: StoreEntity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        _binding = FragmentEditStoreBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getLong(getString(R.string.arg_id), 0)
        if (id != null && id != 0L) {
            mIsEditMode = true
            getStore(id)
        } else {
            mIsEditMode = false
            mStoreEntity = StoreEntity(name = "", phone = "", photoUrl = "")
        }

        setupActionBar()

        setupTextFields()
    }

    private fun setupActionBar() {
        mActivity = activity as? MainActivity
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mActivity?.supportActionBar?.title = if (mIsEditMode) getString(R.string.edit_store_title_edit)
                                            else getString(R.string.edit_store_title_add)

        setHasOptionsMenu(true)
    }

    private fun setupTextFields() {
        /* Lo que metamos entre las llaves de los siguienes métodos, se ejecutará después de
         que cambiemos el texto de sus respectivos editText (etPhotoUrl, etPhone y etName) */
        binding.etPhotoUrl.addTextChangedListener {
            validateFields(binding.tilPhotoUrl)
            loadImage(it.toString().trim())  // <-- Aquí el "it" es "binding.etPhotoUrl.text"
        }

        binding.etPhone.addTextChangedListener {
            validateFields(binding.tilPhone)
        }

        binding.etName.addTextChangedListener {
            validateFields(binding.tilName)
        }
    }

    private fun loadImage(url: String) {
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(binding.ivPhoto)
    }

    private fun getStore(id: Long) {
        doAsync {
            mStoreEntity = StoreApplication.database.storeDao().getStoreById(id)
            uiThread { if (mStoreEntity != null) setUiStore(mStoreEntity!!) }
        }
    }

    private fun setUiStore(storeEntity: StoreEntity) {
        with(binding) {
            // Dos formas de hacer lo siguiente: Comentado-> usando el setter y Descomentado-> con una función de extensión
            //etName.setText(storeEntity.name)
            etName.text = storeEntity.name.editable()
            //etPhone.setText(storeEntity.phone)
            etPhone.text = storeEntity.phone.editable()
            //etWebsite.setText(storeEntity.website)
            etWebsite.text = storeEntity.website.editable()
            //etPhotoUrl.setText(storeEntity.photoUrl)
            etPhotoUrl.text = storeEntity.photoUrl.editable()
            // La foto ya se carga automáticamente con el "addTextChangedListener" 22 líneas más arriba
        }
    }

    private fun String.editable(): Editable = Editable.Factory.getInstance().newEditable(this)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                mActivity?.onBackPressed()
                true
            }
            R.id.action_save -> {
                if (mStoreEntity != null && validateFields(binding.tilPhotoUrl, binding.tilPhone, binding.tilName)) {
                    /*val store = StoreEntity(name = binding.etName.text.toString().trim(),
                    phone = binding.etPhone.text.toString().trim(),
                    website = binding.etWebsite.text.toString().trim(),
                    photoUrl = binding.etPhotoUrl.text.toString().trim())*/
                    with(mStoreEntity!!) {
                        name = binding.etName.text.toString().trim()
                        phone = binding.etPhone.text.toString().trim()
                        website = binding.etWebsite.text.toString().trim()
                        photoUrl = binding.etPhotoUrl.text.toString().trim()
                    }

                    doAsync {
                        if (mIsEditMode) StoreApplication.database.storeDao().updateStore(mStoreEntity!!)
                        else mStoreEntity!!.id = StoreApplication.database.storeDao().addStore(mStoreEntity!!)

                        uiThread {
                            hideKeyboard()

                            if (mIsEditMode) {
                                mActivity?.updateStore(mStoreEntity!!)

                                Snackbar.make(binding.root, R.string.edit_store_message_update_success, Snackbar.LENGTH_SHORT).show()
                            } else {
                                mActivity?.addS:tore(mStoreEntity!!)

                                //Snackbar.make(binding.root, R.string.edit_store_message_save_success, Snackbar.LENGTH_SHORT).show()
                                Toast.makeText(mActivity, R.string.edit_store_message_save_success, Toast.LENGTH_SHORT).show()

                                mActivity?.onBackPressed()
                            }
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun validateFields(vararg textFields: TextInputLayout): Boolean {
        var isValid = true

        for (texField in textFields) {
            if (texField.editText?.text.toString().trim().isEmpty()) {
                texField.error = getString(R.string.helper_required)
                texField.editText?.requestFocus()
                isValid = false
            } else {
                texField.error = null
            }
        }

        if (!isValid) Snackbar.make(binding.root, R.string.edit_store_message_valid, Snackbar.LENGTH_SHORT).show()

        return isValid
    }

    /* Otra forma más larga de crear la función anterior (aquí sin pasar parámetros):
    private fun validateFields(): Boolean {
        var isValid = true

        if (binding.etPhotoUrl.text.toString().trim().isEmpty()) {
            binding.tilPhotoUrl.error = getString(R.string.helper_required)
            binding.etPhotoUrl.requestFocus()
            isValid = false
        }
        if (binding.etPhone.text.toString().trim().isEmpty()) {
            binding.etPhone.error = getString(R.string.helper_required)
            binding.etPhone.requestFocus()
            isValid = false
        }
        if (binding.etName.text.toString().trim().isEmpty()) {
            binding.etName.error = getString(R.string.helper_required)
            binding.etName.requestFocus()
            isValid = false
        }

        if (!isValid) Snackbar.make(binding.root, R.string.edit_store_message_valid, Snackbar.LENGTH_SHORT).show()

        return isValid
    }*/

    private fun hideKeyboard() {
        val imm = mActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (view != null) {
            imm.hideSoftInputFromWindow(view!!.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        hideKeyboard()
        super.onDestroyView()
    }

    override fun onDestroy() {
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mActivity?.supportActionBar?.title = getString(R.string.app_name)
        mActivity?.hideFab(true)

        setHasOptionsMenu(false)
        super.onDestroy()
    }
}


// URLs de prueba:

// https://www.barrabes.com/
// https://pbs.twimg.com/profile_images/498816808703373314/q2Vqtfu7_400x400.png

// https://www.toysrus.es/
// https://cronicaglobal.elespanol.com/uploads/s1/82/58/69/toys-r-us.png

// https://www.lindt.fr/
// http://cdt40.media.tourinsoft.eu/upload/Maison-des-maitres-chocolatiers-LINDT-I--Clement-Herbaux-.jpg

// https://www.cangasaventura.com/wp-content/uploads/2019/06/Imagen-reducida-para-web.jpg
// https://i.pinimg.com/originals/d7/a5/16/d7a516363a656775ecffc96bdb473683.png
// https://jjgomezcaza.es/wp-content/uploads/2019/05/rececho-de-sarrio.png
// https://p-guara.com/