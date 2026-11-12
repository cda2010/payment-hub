(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailsLotDetailController', DetailsLotDetailController);

        DetailsLotDetailController.$inject = ['$scope','$timeout','$uibModalInstance', '$rootScope', '$stateParams','$uibModal', 'entity', 'Lot','DetailsLot','Utils'];

    function DetailsLotDetailController($timeout,$scope,$uibModalInstance, $rootScope, $stateParams,$uibModal, entity, Lot,DetailsLot,Utils) {
        var vm = this;
        // var URL = BASE_URL_API + "etat/factures/listing";
        vm.lot = entity;
        vm.clear = clear;
        // vm.generateFacture=generateFacture;
        // vm.addProduit=addProduit;
        // vm.exporterFactureFacturier= exporterFactureFacturier;
        // console.log('facture',vm.factures);
        // vm.format=null;
        // vm.detailsLots=DetailsLot.
        // queryByFacturier({id:vm.lot.id});
        // vm.selectedProduit=null;
        vm.deleteDetail = deleteDetail
        // vm.itemSearch = {
        //     "refFacture":null,
        //     "refPaiement":null,
        //     "produit":null,
        //     "datePaiement":null,
        //     "format": null,
        //     "facturierId": null
        // };

        function deleteDetail(){
            console.log("supprimer");
        }

//         function exporterFactureFacturier() {
//             vm.itemSearch.facturierId = vm.facturier.id;
//             vm.itemSearch.format=vm.format;
//             if(vm.format===null || vm.format===''){
//                 vm.itemSearch.format = 'PDF';
//                 console.log('facture',vm.itemSearch);
//             }
//             if(vm.itemSearch.format ==='PDF' || vm.format ===null)
//             {
//                 Utils.postPrintUrl(URL,vm.itemSearch);
//             } else {
//                 if(vm.itemSearch.format ==='XLSX') {vm.name='ExportFacture.xlsx';}
//                 if(vm.itemSearch.format ==='CSV') {vm.name='ExportFacture.csv';}
//                 Utils.downloadFile(URL,vm.itemSearch,vm.name);
//             }
// /*            RequestUtils.ExportFactureCsv.get({
//                 "facturierId":vm.facturier.id,
//             }, function (datas) {
//                 console.log(datas);
//                 Notification.success("Generation effectuée avec succès!Fichier disponible dans votre repertoire utilisateur");
//             });*/
//         }


        // function generateFacture() {
        //     $uibModal.open({
        //         templateUrl: 'app/entities/facture/facture-dialog.html',
        //         controller: 'FactureDialogFacturierController',
        //         controllerAs: 'vm',
        //         backdrop: 'static',
        //         size: 'md',
        //         resolve: {
        //             entity: function () {
        //                 return {
        //                     referenceFacature: null,
        //                     montantFacture: null,
        //                     referencePaiement: null,
        //                     datePaiement: null,
        //                     id: null,
        //                     produit:null,
        //                     produits:vm.produits,
        //                 };
        //             }
        //         }
        //     }).result.then(function() {
        //         Notification.success("Factures genérées avec succès");
        //         vm.factures = Facture.queryByFacturier({id:vm.facturier.id});
        //     }, function() {
        //     });
        // }

      
        // function addClient() {
        //     $uibModal.open({
        //         templateUrl: 'app/entities/lot/lot-dialog.html',
        //         controller: 'LotDialogController',
        //         controllerAs: 'vm',
        //         backdrop: 'static',
        //         size: 'md',
        //         resolve: {
        //             entity: function () {
        //                 return {
        //                     nomProduit: null,
        //                     numeroOrdre: null,
        //                     actif: null,
        //                     facturier: vm.facturier,
        //                     id: null
        //                 };
        //             }
        //         }
        //     }).result.then(function() {
        //         vm.produits=Produit.queryByFacturier({id:vm.facturier.id});
        //         Notification.success("Produit enregistré  avec succès");
        //     }, function() {
        //     });
        // }

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }


    }
})();
