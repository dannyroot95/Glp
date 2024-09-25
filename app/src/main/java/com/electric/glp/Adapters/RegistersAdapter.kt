import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.electric.glp.databinding.ItemRegisterBinding
import com.bumptech.glide.Glide
import com.electric.glp.Models.RegisterData
import com.electric.glp.R
import com.electric.glp.Utils.ConvertTimestampToDateTime
import com.google.firebase.database.DatabaseReference

class RegistersAdapter(private var data: List<RegisterData>, private val databaseRef: DatabaseReference) :
    RecyclerView.Adapter<RegistersAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRegisterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: RegisterData, deleteAction: (String) -> Unit) {
            binding.itGlp.text = "GLP: ${data.glp} ppm"
            binding.itDate.text = "Fecha: "+ConvertTimestampToDateTime().convert(data.timestamp)
            binding.itCO.text = "CO: ${data.co} ppm"
            binding.itSmoke.text = "Humo: ${data.smoke} ppm"

            // Choose image based on the GLP value
            val imageResource = if (data.glp > 10) R.drawable.ic_alert else R.drawable.ic_check
            Glide.with(binding.imageViewStatus.context).load(imageResource).into(binding.imageViewStatus)

            binding.btnDelete.setOnClickListener {

                val builder = AlertDialog.Builder(binding.root.context)
                builder.setTitle("Alerta!")
                builder.setIcon(R.drawable.ic_delete)
                builder.setMessage("¿Estás seguro de eliminar este registro?")

                // Agregar botón de confirmación
                builder.setPositiveButton("Sí") { dialog, which ->
                    Toast.makeText(binding.root.context,"Dato eliminado!",Toast.LENGTH_SHORT).show()
                    deleteAction(data.key)
                }

                // Agregar botón de cancelación
                builder.setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }

                // Mostrar el AlertDialog
                builder.show()

            }

        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRegisterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position]) {
            databaseRef.child(it).removeValue()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = data.size

    // Method to update the data within the adapter
    fun updateData(newData: List<RegisterData>) {
        data = newData
        notifyDataSetChanged() // Notify any registered observers that the data set has changed.
    }
}
