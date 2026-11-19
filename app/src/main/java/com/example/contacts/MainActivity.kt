package com.example.contacts

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // 1) Les données : une liste MODIFIABLE (mutableListOf) de chaînes simples
    private val contacts = mutableListOf(
        "Ahmed Ben Ali",
        "Sarra Trabelsi",
        "Mohamed Gharbi",
        "Ines Jaziri",
        "Youssef Mansouri",
        "Amira Chaabane",
        "Karim Bouazizi",
        "Nour Hammami"
    )

    // 2) L'adaptateur fait le lien entre les données et la ListView
    private lateinit var adapter: ArrayAdapter<String>

    // 3) Les vues
    private lateinit var etContact: EditText
    private lateinit var btnAjouter: Button
    private lateinit var btnModifier: Button
    private lateinit var lvContacts: ListView

    // Position de l'élément sélectionné (-1 = aucune sélection)
    private var positionSelectionnee = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Récupération de l'état sauvegardé (gestion de la rotation d'écran - Exercice 5)
        if (savedInstanceState != null) {
            val savedContacts = savedInstanceState.getStringArrayList("contacts_list")
            if (savedContacts != null) {
                contacts.clear()
                contacts.addAll(savedContacts)
            }
            positionSelectionnee = savedInstanceState.getInt("position_selectionnee", -1)
        } else {
            // Tri initial de la liste par ordre alphabétique (Exercice 4)
            contacts.sort()
        }

        // Récupération des vues déclarées dans activity_main.xml
        etContact = findViewById(R.id.etContact)
        btnAjouter = findViewById(R.id.btnAjouter)
        btnModifier = findViewById(R.id.btnModifier)
        lvContacts = findViewById(R.id.lvContacts)

        // Création de l'adaptateur avec un layout Android prédéfini
        // simple_list_item_activated_1 met en surbrillance l'élément sélectionné
        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_activated_1,
            contacts
        )
        lvContacts.adapter = adapter

        // Restaurer la sélection visuelle après rotation si un élément était sélectionné
        if (positionSelectionnee != -1 && positionSelectionnee < contacts.size) {
            lvContacts.setItemChecked(positionSelectionnee, true)
            etContact.setText(contacts[positionSelectionnee])
        }

        // Clic sur un élément de la liste : on copie son texte dans l'EditText
        lvContacts.setOnItemClickListener { _, _, position, _ ->
            positionSelectionnee = position
            lvContacts.setItemChecked(position, true)
            etContact.setText(contacts[position])
            etContact.setSelection(etContact.text.length) // curseur à la fin
            etContact.requestFocus()
        }

        // Exercice 2 : Clic long sur un élément pour le supprimer avec confirmation (AlertDialog)
        lvContacts.setOnItemLongClickListener { _, _, position, _ ->
            val contactASupprimer = contacts[position]
            AlertDialog.Builder(this)
                .setTitle(R.string.dialog_supprimer_titre)
                .setMessage(getString(R.string.dialog_supprimer_msg, contactASupprimer))
                .setPositiveButton(R.string.dialog_btn_supprimer) { _, _ ->
                    supprimerContact(position)
                }
                .setNegativeButton(R.string.dialog_btn_annuler, null)
                .show()
            true // Consomme l'événement pour éviter de déclencher le clic court
        }

        // Exercice 1 : Clic sur le bouton "Ajouter"
        btnAjouter.setOnClickListener { ajouterContact() }

        // Clic sur le bouton "Modifier"
        btnModifier.setOnClickListener { modifierContact() }

        // Bonus : la touche "Terminé" du clavier valide aussi l'action
        etContact.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (positionSelectionnee == -1) {
                    ajouterContact()
                } else {
                    modifierContact()
                }
                true
            } else {
                false
            }
        }
    }

    // Exercice 1 : Ajouter un nouveau contact
    private fun ajouterContact() {
        val nouveauNom = etContact.text.toString().trim()

        // Contrôle 1 : texte non vide
        if (nouveauNom.isEmpty()) {
            etContact.error = getString(R.string.msg_nom_vide)
            return
        }

        // Exercice 3 : Contrôle doublons
        if (contacts.any { it.equals(nouveauNom, ignoreCase = true) }) {
            Toast.makeText(this, R.string.msg_doublon, Toast.LENGTH_SHORT).show()
            return
        }

        // Insertion du nouveau contact
        contacts.add(nouveauNom)

        // Exercice 4 : Tri alphabétique après modification
        contacts.sort()

        // Mise à jour de l'affichage
        adapter.notifyDataSetChanged()

        // Réinitialisation de la saisie et de la sélection
        reinitialiserSelection()

        Toast.makeText(this, getString(R.string.msg_contact_ajoute, nouveauNom), Toast.LENGTH_SHORT).show()
    }

    // Modification d'un contact existant
    private fun modifierContact() {
        // Contrôle 1 : un élément doit être sélectionné
        if (positionSelectionnee == -1) {
            Toast.makeText(this, R.string.msg_aucune_selection, Toast.LENGTH_SHORT).show()
            return
        }

        // Contrôle 2 : le texte ne doit pas être vide
        val nouveauNom = etContact.text.toString().trim()
        if (nouveauNom.isEmpty()) {
            etContact.error = getString(R.string.msg_nom_vide)
            return
        }

        // Exercice 3 : Contrôle doublons (refuser si le nom existe déjà ailleurs dans la liste)
        val doublon = contacts.indices.any { i ->
            i != positionSelectionnee && contacts[i].equals(nouveauNom, ignoreCase = true)
        }
        if (doublon) {
            Toast.makeText(this, R.string.msg_doublon, Toast.LENGTH_SHORT).show()
            return
        }

        // Mise à jour de la donnée
        contacts[positionSelectionnee] = nouveauNom

        // Exercice 4 : Tri alphabétique après chaque modification
        contacts.sort()

        // Retrouver la nouvelle position du contact après le tri pour maintenir la sélection
        positionSelectionnee = contacts.indexOf(nouveauNom)
        if (positionSelectionnee != -1) {
            lvContacts.setItemChecked(positionSelectionnee, true)
        }

        // Prévenir l'adaptateur pour redessiner la liste
        adapter.notifyDataSetChanged()

        Toast.makeText(this, "Contact modifié : $nouveauNom", Toast.LENGTH_SHORT).show()
    }

    // Exercice 2 : Supprimer un contact
    private fun supprimerContact(position: Int) {
        val contactSupprime = contacts.removeAt(position)

        // Si l'élément supprimé était celui sélectionné, on réinitialise
        if (positionSelectionnee == position) {
            reinitialiserSelection()
        } else if (positionSelectionnee > position) {
            positionSelectionnee--
            lvContacts.setItemChecked(positionSelectionnee, true)
        }

        adapter.notifyDataSetChanged()
        Toast.makeText(this, "${getString(R.string.msg_contact_supprime)} : $contactSupprime", Toast.LENGTH_SHORT).show()
    }

    private fun reinitialiserSelection() {
        positionSelectionnee = -1
        etContact.text.clear()
        lvContacts.clearChoices()
    }

    // Exercice 5 : Sauvegarde de l'état pour survivre à la rotation de l'écran
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("contacts_list", ArrayList(contacts))
        outState.putInt("position_selectionnee", positionSelectionnee)
    }
}
