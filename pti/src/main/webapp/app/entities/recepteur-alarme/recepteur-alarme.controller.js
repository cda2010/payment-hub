(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RecepteurAlarmeController', RecepteurAlarmeController);

    RecepteurAlarmeController.$inject = ['$state', 'RecepteurAlarme', '$scope', 'TableService'];

    function RecepteurAlarmeController($state, RecepteurAlarme, $scope, TableService) {

        var vm = this;
        init();
        vm.afficheFiltre = false;
        vm.itemSearch = {
            "etiquette": null,
            "sms": null
        };
        vm.afficherFiltre=function () {
            vm.afficheFiltre = true;
        };
        vm.chercherAlarme = function () {
            init();
        };
        vm.annulerAlarme = function () {
            vm.itemSearch = {
                "etiquette": null,
                "sms": null
            };
        };
        function init(){
            var addItemData = {
                templateUrl: 'app/entities/recepteur-alarme/recepteur-alarme-dialog.html',
                controller: 'RecepteurAlarmeDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
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
                            etiquette: null,
                            emails: null,
                            sms: null,
                            id: null
                        };
                    }
                }
            };

            var editItemData = {
                templateUrl: 'app/entities/recepteur-alarme/recepteur-alarme-dialog.html',
                controller: 'RecepteurAlarmeDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
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
                templateUrl: 'app/entities/recepteur-alarme/recepteur-alarme-dialog.html',
                controller: 'RecepteurAlarmeDialogController',
                controllerAs: 'vm',
                size: 'lg',
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
                templateUrl: 'app/entities/recepteur-alarme/recepteur-alarme-delete-dialog.html',
                controller: 'RecepteurAlarmeDeleteController',
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
                    "sms": vm.itemSearch.sms
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: RecepteurAlarme,
                dialogData: dialogData,
                entitySearch: RecepteurAlarme,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams,
                loadCallBack: vm.load
            };
            TableService.init(datas);

        }

    }
})();
