(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ProduitController', ProduitController);

    ProduitController.$inject = ['$scope','$state','Produit','Ecran','TableService','$uibModal','$stateParams'];

    function ProduitController($scope,$state,Produit,Ecran,TableService,$uibModal,$stateParams) {

        var vm = this;

        vm.produits = [];
        vm.produitEcrans=[];
        vm.selectedProduit=null;
        vm.init=init;
        vm.findEcransOfProduit=findEcransOfProduit;
        vm.goBack=false;
        vm.canShowResultCount=false;
        vm.viewEcran=viewEcran;
        vm.addEcran=addEcran;
        vm.editEcran=editEcran;
        vm.deleteEcran=deleteEcran;
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
            }, function() {
            });
        }
        function findEcransOfProduit(produit) {
        vm.produitEcrans=Ecran.queryByProduit({id:produit.id});
        vm.selectedProduit=produit;
        //console.log(vm.produitEcrans);
        }

        loadAll();

        function loadAll() {
            if($stateParams.idFacturier){
                vm.produits = Produit.queryByFacturier({id:$stateParams.idFacturier});
                vm.goBack=true;
                console.log('ok');
            }else {
                init();
                Produit.query(function(result) {
                    vm.produits = result;
                    vm.searchQuery = null;
                    vm.totalItems = result.length + 1;
                });
            }

        }

        function init(){
            var addItemData = {
                templateUrl: 'app/entities/produit/produit-dialog.html',
                controller: 'ProduitDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: function () {
                        return {

                        };
                    }
                }
            };

            var editItemData = {
                templateUrl: 'app/entities/produit/produit-dialog.html',
                controller: 'ProduitDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return true;
                    },
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };

            var detailItemData = {
                templateUrl: 'app/entities/produit/produit-dialog.html',
                controller: 'ProduitDialogController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return true;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: null //valeur déterminé à l'appel de la boite,
                }
            };
            var deleteItemData = {
                templateUrl: 'app/entities/produit/produit-delete-dialog.html',
                controller: 'ProduitDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };
            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                detailData: detailItemData,
                deleteData: deleteItemData
            };
            var entitySearchParams = function () {
                return {};
            };
            var entityParams = function () {
                return {

                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: Produit,
                dialogData: dialogData,
                entitySearch: Produit,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams,
                loadCallBack: vm.load
            };
            console.log('resultat',datas);
            TableService.init(datas);
            vm.canShowResultCount = true;

        }
    }
})();
