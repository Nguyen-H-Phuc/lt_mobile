package com.example.project183.Helper;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.content.Context;
import android.widget.Toast;
import com.example.project183.Domain.Foods;
import java.util.ArrayList;
public class ManagmentCart {
    private final Context context;
    private final TinyDB tinyDB;
    private final DatabaseReference databaseReference;

    public ManagmentCart(Context context) {
        this.context = context;
        this.tinyDB=new TinyDB(context);
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Carts");
    }

    public void insertFood(Foods item) {
        ArrayList<Foods> listpop = getListCart();
        boolean existAlready = false;
        int n = 0;
        for (int i = 0; i < listpop.size(); i++) {
            if (listpop.get(i).getTitle().equals(item.getTitle())) {
                existAlready = true;
                n = i;
                break;
            }
        }
        if(existAlready){
            listpop.get(n).setNumberInCart(item.getNumberInCart());
        }else{
            listpop.add(item);
        }
        tinyDB.putListObject("CartList",listpop);
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show();
    }

    public ArrayList<Foods> getListCart() {
        return tinyDB.getListObject("CartList");
    }

    public Double getTotalFee(){
        ArrayList<Foods> listItem=getListCart();
        double fee=0;
        for (int i = 0; i < listItem.size(); i++) {
            fee=fee+(listItem.get(i).getPrice()*listItem.get(i).getNumberInCart());
        }
        return fee;
    }
    public void minusNumberItem(ArrayList<Foods> listItem,int position,ChangeNumberItemsListener changeNumberItemsListener){
        if(listItem.get(position).getNumberInCart()==1){
            listItem.remove(position);
        }else{
            listItem.get(position).setNumberInCart(listItem.get(position).getNumberInCart()-1);
        }
        tinyDB.putListObject("CartList",listItem);
        changeNumberItemsListener.change();
    }
    public  void plusNumberItem(ArrayList<Foods> listItem,int position,ChangeNumberItemsListener changeNumberItemsListener){
        listItem.get(position).setNumberInCart(listItem.get(position).getNumberInCart()+1);
        tinyDB.putListObject("CartList",listItem);
        changeNumberItemsListener.change();
    }
    public  void removeItem(ArrayList<Foods> listItem,int position,ChangeNumberItemsListener changeNumberItemsListener){
        listItem.remove(position);
        tinyDB.putListObject("CartList",listItem);
        changeNumberItemsListener.change();
    }

    public void writeCartToFirebase(String userId) {
        ArrayList<Foods> cartList = getListCart();

        if (cartList != null && !cartList.isEmpty()) {
            databaseReference.child(userId).setValue(cartList)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Đã ghi giỏ hàng lên Firebase", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi khi ghi Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "Giỏ hàng trống, không ghi lên Firebase", Toast.LENGTH_SHORT).show();
        }
    }

}
