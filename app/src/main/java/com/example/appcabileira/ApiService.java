package com.example.appcabileira;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("login.php")
    Call<ApiResponse> login(@Field("email") String email, @Field("senha") String senha);

    @FormUrlEncoded
    @POST("salvar_local.php")
    Call<ApiResponse> salvarLocal(
            @Field("nome_local") String nome,
            @Field("endereco") String endereco,
            @Field("latitude") double lat,
            @Field("longitude") double lng,
            @Field("email") String email);
    @FormUrlEncoded
    @POST("registro.php")
    Call<ApiResponse> registrar(
            @Field("nome") String nome,
            @Field("sobrenome") String sobrenome,
            @Field("email") String email,
            @Field("telefone") String telefone,
            @Field("cpf") String cpf,
            @Field("senha") String senha,
            @Field("tipo_usuario") String tipoUsuario
    );
    @FormUrlEncoded
    @POST("listar_locais.php")
    Call<java.util.List<LocalServico>> listarLocais(@Field("email") String email);

    @FormUrlEncoded
    @POST("atualizar_local.php")
    Call<ApiResponse> atualizarLocal(
            @Field("id") int id,
            @Field("nome_local") String nome,
            @Field("endereco") String endereco,
            @Field("latitude") double lat,
            @Field("longitude") double lng
    );

    @FormUrlEncoded
    @POST("excluir_local.php")
    Call<ApiResponse> excluirLocal(@Field("id") int id);

    @GET("buscar_todos_locais.php")
    Call<java.util.List<LocalServico>> buscarTodosLocais();
}