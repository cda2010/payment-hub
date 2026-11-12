(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CompteDetailController', CompteDetailController);

    CompteDetailController.$inject = ['$uibModalInstance', '$scope', 'TableService', 'entity', 'RequestUtils'];

    function CompteDetailController($uibModalInstance, $scope, TableService, entity, RequestUtils) {
        var vm = this;

        vm.compte = entity;
        vm.close = clear;
        vm.openCalendar = openCalendar;
        vm.datePickerOpenStatus = {};
        vm.itemSearch = {
            dateDebut: null,
            dateFin: null
        };

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        vm.initFiltre = function () {
            vm.filtrer = !vm.filtrer;
            if (!vm.filtrer) {
                vm.itemSearch = {
                    "dateDebut": null,
                    "dateFin": null
                };
                init();
            }
        }

        vm.filtre = function () {
            console.log("filtre des informations en cours");
            init();
        }

        function openCalendar(date) {
            vm.datePickerOpenStatus[date] = true;
        }

        vm.soldeBilan;
        console.log("Compte autre bilan ou ......");

        RequestUtils.BilanCompteAutre.get({
            compteAutreId: vm.compte.compteAutreId
        }, function (datas) {
            console.log("Compte autre bilan ......");
            console.log(datas);
           vm.soldeBilan=parseFloat(datas.montant);
        });
        
        function init() {
            var deleteItemData = {
                templateUrl: 'app/entities/transaction-compte-pti/transaction-compte-pti-delete-dialog.html',
                controller: 'TransactionComptePtiDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };

            var dialogData = {
                deleteData: deleteItemData
            };

            var entitySearchParams = function () {
                return {};
            };
           

            var entityParams = function () {
                return {
                    compteAutreId: vm.compte.compteAutreId,
                    dateDebut: vm.itemSearch.dateDebut,
                    dateFin: vm.itemSearch.dateFin
                };
            };

            var datas = {
                scope: $scope,
                vm: vm,
                entity: RequestUtils.TransationByCompteAutre,
                dialogData: dialogData,
                entitySearch: RequestUtils.TransationByCompteAutre,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
        }
        init();
    }
})();