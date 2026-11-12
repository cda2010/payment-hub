(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CompteController', CompteController);

    CompteController.$inject = ['RequestUtils','$rootScope', 'ChoisirUo', 'EtiquetteCompte', 'TableService', 'Compte', '$scope'];

    function CompteController(RequestUtils,$rootScope, ChoisirUo, EtiquetteCompte, TableService, Compte, $scope) {
        var vm = this;
        vm.etiquettes = RequestUtils.EtiquetteCompteList.query();
        vm.filtrer = false;
        vm.itemSearch = {
            "organisationId": null,
            "etiquette": null,
            "categorieCompte":null,
            "valeurCompte": null,
        };

        vm.initFiltre = function () {
            vm.filtrer = !vm.filtrer;
            if (!vm.filtrer) {
                vm.itemSearch = {
                    "organisationId": null,
                    "etiquette": null,
                    "categorieCompte":null,
                    "valeurCompte": null
                };
                vm.organisation = null;
                init();
            }
        }

        vm.organisation = null;
        vm.choisirUo = function () {
            console.log("choisir uo");
            ChoisirUo.open($scope);
        };

        var unsubscribe = $rootScope.$on('ptiApp:choisirUo', function (event, result) {
            console.log("uo choisi");
            if (result != null) {
                vm.organisation = result;
                vm.itemSearch.organisationId = result.id;
            }
        });

        $scope.$on('$destroy', unsubscribe);


        vm.filtre = function () {
            console.log("filtre des informations en cours");
            init();
        };

        init();
        function init() {
            var addItemData = {
                templateUrl: 'app/entities/compte/compte-dialog.html',
                controller: 'CompteDialogController',
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
                            /*Initialisation des valeurs par défauts de l'entité à créer*/
                            id: null,
                            etiquette: null,
                            valeurCompte: null

                        };
                    }
                }
            };

            var editItemData = {
                templateUrl: 'app/entities/compte/compte-dialog.html',
                controller: 'CompteDialogController',
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
                templateUrl: 'app/entities/compte/compte-dialog.html',
                controller: 'CompteDialogController',
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
                templateUrl: 'app/entities/compte/compte-delete-dialog.html',
                controller: 'CompteDeleteController',
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
                    "etiquette": vm.itemSearch.etiquette,
                    "uoId": vm.itemSearch.organisationId,
                    "categorieCompte":vm.itemSearch.categorieCompte,
                    "valeurCompte": vm.itemSearch.valeurCompte
                };
            };


            var datas = {
                scope: $scope,
                vm: vm,
                entity: Compte,
                dialogData: dialogData,
                entitySearch: Compte,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
            console.log('all compte',datas);
        }


    }

})();
