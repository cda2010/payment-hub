(function () {
    'use strict';

    angular
            .module('ptiApp')
            .controller('PtiJobDetailsController', PtiJobDetailsController);

    PtiJobDetailsController.$inject = ['$state', '$scope', 'PtiJobDetails', 'ParseLinks', 'AlertService', 'TableService'];

    function PtiJobDetailsController($state, $scope, PtiJobDetails, ParseLinks, AlertService, TableService) {

        var vm = this;
        vm.ini = ini;
        ini();
        function ini() {
            var addItemData = {
                templateUrl: 'app/entities/pti-job-details/pti-job-details-dialog.html',
                controller: 'PtiJobDetailsDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return {
                            jobName: null,
                            displayName: null,
                            expressionCron: null,
                            priorite: null,
                            schedulerGroupe: null,
                            nomGroupe: null,
                            dateDerniereExecution: null,
                            dateProchaineExecution: null,
                            jobKey: null,
                            erreurExecution: null,
                            estActif: null,
                            estEnExecution: null,
                            fournisseurServiceId: null,
                            jobClass: null,
                            cronJob: null,
                            repeatTime:null,
                            id: null
                        }
                    },
                    isViewing: false
                }
            };

            /**
             *Modification d'un élément
             */
            var editItemData = {
                templateUrl: 'app/entities/pti-job-details/pti-job-details-dialog.html',
                controller: 'PtiJobDetailsDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: null,
                    isViewing: false
                }
            };

            /**
             * Suppresion d'un élément
             */
            var deleteItemData = {
                templateUrl: 'app/entities/pti-job-details/pti-job-details-delete-dialog.html',
                controller: 'PtiJobDetailsDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null
                }
            };

            var detailItemData = {
                templateUrl: 'app/entities/pti-job-details/pti-job-details-dialog.html',
                controller: 'PtiJobDetailsDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: null, //valeur déterminé à l'appel de la boite
                    isViewing: true
                }
            };

            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                deleteData: deleteItemData,
                detailData: detailItemData
            };
            var entitySearchParams = function () {
                return {
                };
            };

            var entityParams = function () {
                return {
                };
            };

            var datas = {
                scope: $scope,
                vm: vm,
                entity: PtiJobDetails,
                entitySearch: PtiJobDetails,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
        }

    }
})();
