(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('VerifTransactionController', VerifTransactionController);

        VerifTransactionController.$inject = ['$state', 'VerifTransaction','Facturier', '$scope','$uibModal','BASE_URL_API','Utils','RequestUtils','Produit'];

    function VerifTransactionController($state, VerifTransaction,Facturier, $scope, $uibModal,BASE_URL_API,Utils,RequestUtils,Produit) {
        var vm = this;
        var URL = BASE_URL_API + "etat/";
        vm.facturiers = Facturier.query();
        vm.canShowResultCount=false;
        vm.filtreCtrl=false;
        vm.format=null;
        vm.afficheFiltre = false;
       

        vm.itemSearch = {
            "produit": null,
            "code": null,
            "facturier": null
        };
        vm.filtrer = true;
        vm.afficherFiltre= afficherFiltre;

        function afficherFiltre(){
            vm.afficheFiltre=!vm.afficheFiltre;
            if (!vm.afficheFiltre) {
                vm.itemSearch = {
                    "produit": null,
                    "code": null,
                    "facturier": null
                };
            }
        }

       vm.filtre = function () {
            var idProduit = vm.itemSearch.produit;
            var mparametre = [
                { codeParametre: "REFERENCE", valeur: vm.code, affiche: 1 } 
            ];
            VerifTransaction.queryByParamProduit({ idProduit: idProduit, mparametre: mparametre }
            ,function(response) {
                vm.data = response;
               // vm.data = [];
            }, function(error) {
                console.error('Erreur lors de la récupération des données:', error);
             });
             vm.afficheFiltre = true;
        };


        
        vm.changeFacturier = function(){
            vm.produits=Produit.queryByFacturier({id:vm.itemSearch.facturier});
          //  init();
        };
        function init() {

            var addItemData = {};

            /**
             *Modification d'un élément
             */
            var editItemData = {};

            /**
             * Suppresion d'un élément
             */
            var deleteItemData = {};

            var detailItemData = {};

            var dialogData = { 
                addData: addItemData,
                editData: editItemData,
                deleteData: deleteItemData,
                detailData: detailItemData
            };
            var entitySearchParams = function () {
                return {};
            };


            var entityParams = function () {
                return {
                    "produit": null,
                    "code": null,
                    "facturier": null
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: RequestUtils.GetListClientOptimise,
                entitySearch: RequestUtils.GetListClientOptimise,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
            vm.canShowResultCount = true;
        }

    }
})();
