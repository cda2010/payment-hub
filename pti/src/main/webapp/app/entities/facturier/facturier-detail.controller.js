(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FacturierDetailController', FacturierDetailController);

    FacturierDetailController.$inject = ['$scope','$timeout','$uibModalInstance', '$rootScope', '$stateParams','$uibModal', 'entity', 'Facturier', 'Facture','Produit','Notification','Ecran', 'BASE_URL_API','Utils'];

    function FacturierDetailController($timeout,$scope,$uibModalInstance, $rootScope, $stateParams,$uibModal, entity, Facturier, Facture,Produit,Notification, Ecran, BASE_URL_API,Utils) {
        var vm = this;
        var URL = BASE_URL_API + "etat/factures/listing";
        vm.facturier = entity;
        vm.clear = clear;
        vm.generateFacture=generateFacture;
        vm.addProduit=addProduit;
        vm.exporterFactureFacturier= exporterFactureFacturier;
        console.log('facture',vm.factures);
        vm.format=null;
        vm.factures = Facture.queryByFacturier({id:vm.facturier.id});
        vm.produits=Produit.queryByFacturier({id:vm.facturier.id});
        vm.selectedProduit=null;
        vm.produitEcrans=[];
        vm.viewEcran=viewEcran;
        vm.addEcran=addEcran;
        vm.editEcran=editEcran;
        vm.deleteEcran=deleteEcran;
        vm.findEcransOfProduit=findEcransOfProduit;
        vm.itemSearch = {
            "refFacture":null,
            "refPaiement":null,
            "produit":null,
            "datePaiement":null,
            "format": null,
            "facturierId": null
        };
        function exporterFactureFacturier() {
            vm.itemSearch.facturierId = vm.facturier.id;
            vm.itemSearch.format=vm.format;
            if(vm.format===null || vm.format===''){
                vm.itemSearch.format = 'PDF';
                console.log('facture',vm.itemSearch);
            }
            if(vm.itemSearch.format ==='PDF' || vm.format ===null)
            {
                Utils.postPrintUrl(URL,vm.itemSearch);
            } else {
                if(vm.itemSearch.format ==='XLSX') {vm.name='ExportFacture.xlsx';}
                if(vm.itemSearch.format ==='CSV') {vm.name='ExportFacture.csv';}
                Utils.downloadFile(URL,vm.itemSearch,vm.name);
            }
/*            RequestUtils.ExportFactureCsv.get({
                "facturierId":vm.facturier.id,
            }, function (datas) {
                console.log(datas);
                Notification.success("Generation effectuée avec succès!Fichier disponible dans votre repertoire utilisateur");
            });*/
        }
        function viewEcran(idEcran) {
            $uibModal.open({
                templateUrl: 'app/entities/ecran/ecran-detail.html',
                controller: 'EcranDetailController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: ['Ecran', function(Ecran) {
                        return Ecran.get({id : idEcran}).$promise;
                    }]
                }
            }).result.then(function() {
                findEcransOfProduit(vm.selectedProduit);
            }, function() {
            });
        }
        function  addEcran() {
            $uibModal.open({
                templateUrl: 'app/entities/ecran/ecran-dialog.html',
                controller: 'EcranDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return {
                            numeroOrdre: null,
                            id: null,
                            produit:vm.selectedProduit
                        };
                    }
                }
            }).result.then(function() {
                findEcransOfProduit(vm.selectedProduit);
                Notification.success("Ecran ajouté avec succès");
            }, function() {

            });
        }
        function editEcran(idEcran) {
            $uibModal.open({
                templateUrl: 'app/entities/ecran/ecran-dialog.html',
                controller: 'EcranDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: ['Ecran', function(Ecran) {
                        return Ecran.get({id : idEcran}).$promise;
                    }]
                }
            }).result.then(function() {
                findEcransOfProduit(vm.selectedProduit);
                Notification.success("Ecran Modifié avec succès");
            }, function() {

            });
        }
        function  deleteEcran(idEcan) {
            $uibModal.open({
                templateUrl: 'app/entities/ecran/ecran-delete-dialog.html',
                controller: 'EcranDeleteController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    entity: ['Ecran', function(Ecran) {
                        return Ecran.get({id : idEcan}).$promise;
                    }]
                }
            }).result.then(function() {
                findEcransOfProduit(vm.selectedProduit);
                Notification.success("Ecran supprimé avec succès");
            }, function() {
            });
        }
        function findEcransOfProduit(produit) {
            vm.produitEcrans=Ecran.queryByProduit({id:produit.id});
            vm.selectedProduit=produit;
            //console.log(vm.produitEcrans);
        }

        function generateFacture() {
            $uibModal.open({
                templateUrl: 'app/entities/facture/facture-dialog.html',
                controller: 'FactureDialogFacturierController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return {
                            referenceFacature: null,
                            montantFacture: null,
                            referencePaiement: null,
                            datePaiement: null,
                            id: null,
                            produit:null,
                            produits:vm.produits,
                        };
                    }
                }
            }).result.then(function() {
                Notification.success("Factures genérées avec succès");
                vm.factures = Facture.queryByFacturier({id:vm.facturier.id});
            }, function() {
            });
        }
        vm.editFacture=function (idFacture) {
            $uibModal.open({
                templateUrl: 'app/entities/facture/facture-dialog.html',
                controller: 'FactureDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: ['Facture', function(Facture) {
                        return Facture.get({id : idFacture}).$promise;
                    }]
                }
            }).result.then(function() {
                vm.factures = Facture.queryByFacturier({id:vm.facturier.id});
                Notification.success("Facture modifiée avec succès");
            }, function() {
            });

        }
        vm.deleteFacture=function (idFacture) {
            $uibModal.open({
                templateUrl: 'app/entities/facture/facture-delete-dialog.html',
                controller: 'FactureDeleteController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    entity: ['Facture', function(Facture) {
                        return Facture.get({id : idFacture}).$promise;
                    }]
                }
            }).result.then(function() {
                vm.factures = Facture.queryByFacturier({id:vm.facturier.id});
                Notification.success("Facture supprimée avec succès");
            }, function() {
            });
        }
        function addProduit() {
            console.log('added produit---');
            $uibModal.open({
                templateUrl: 'app/entities/produit/produit-dialog.html',
                controller: 'ProduitDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return {
                            nomProduit: null,
                            numeroOrdre: null,
                            actif: null,
                            facturier: vm.facturier,
                            id: null
                        };
                    }
                }
            }).result.then(function() {
                vm.produits=Produit.queryByFacturier({id:vm.facturier.id});
                Notification.success("Produit enregistré  avec succès");
            }, function() {
            });
        }
        vm.editProduit= function(idProduit){
            $uibModal.open({
                templateUrl: 'app/entities/produit/produit-dialog.html',
                controller: 'ProduitDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: ['Produit', function(Produit) {
                        return Produit.get({id :idProduit}).$promise;
                    }]
                }
            }).result.then(function() {
                vm.produits=Produit.queryByFacturier({id:vm.facturier.id});
                Notification.success("Produit modifié avec succès");
            }, function() {

            });
        }

        vm.deleteProduit=function (idProduit) {
            $uibModal.open({
                templateUrl: 'app/entities/produit/produit-delete-dialog.html',
                controller: 'ProduitDeleteController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    entity: ['Produit', function(Produit) {
                        return Produit.get({id : idProduit}).$promise;
                    }]
                }
            }).result.then(function() {
                vm.produits=Produit.queryByFacturier({id:vm.facturier.id});
                Notification.success("Produit supprimé avec succès");
            }, function() {

            });
        }

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }


    }
})();
