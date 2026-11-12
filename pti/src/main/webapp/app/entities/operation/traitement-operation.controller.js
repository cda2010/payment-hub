(function () {
    'use strict';

    angular
            .module('ptiApp')
            .controller('TraitementOperationController', TraitementOperationController);

    TraitementOperationController.$inject = ['$uibModal', 'RequestUtils', 'Canal', 'TableService', '$scope', '$state', 'Operation', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams', 'typeService'];

    function TraitementOperationController($uibModal, RequestUtils, Canal, TableService, $scope, $state, Operation, ParseLinks, AlertService, paginationConstants, pagingParams, typeService) {
        var vm = this;
        //vm.typeServices = RequestUtils.SomeTypeService.query();
        //vm.typeServices = RequestUtils.SomeTypeService.query();
        vm.typeService = typeService;
        vm.canals = Canal.query();
        vm.filtrer = false;
        vm.itemSearch = {
            "statutOperation": "ENATTENTE",
            "canalId": null,
            "typeService": vm.typeService,
            "montantOp": null,
            "dateOperation": null,
            "uoAcquereurId": null
        };
        vm.openCalendar = openCalendar;
        vm.datePickerOpenStatus = {};
        vm.datePickerOpenStatus.dateOperation = false;

        function openCalendar(date) {
            console.log("open");
            vm.datePickerOpenStatus[date] = true;
        }

        vm.initFiltre = function () {
            vm.filtrer = !vm.filtrer;
            if (!vm.filtrer) {
                vm.itemSearch = {
                    "statutOperation": "ENATTENTE",
                    "canalId": null,
                    "typeService": vm.typeService,
                    "montantOp": null,
                    "dateOperation": null,
                    "uoAcquereurId": null
                };
                init();
            }
        }

        vm.filtre = function () {
            console.log("filtre des informations en cours");
            init();
        }
        RequestUtils.GetOrganisationByType.get({type: "PAYS"}, function (datas) {
            vm.pays = datas;
        });
        init();

        function init() {
            var detailItemData = {
                templateUrl: 'app/entities/operation/operation-dialog.html',
                controller: 'OperationDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: null, //valeur déterminé à l'appel de la boite
                    isViewing: true
                }
            };

            var dialogData = {
                detailData: detailItemData
            };
            var entitySearchParams = function () {
                return {};
            };


            var entityParams = function () {
                return {
                    "statutOperation": vm.itemSearch.statutOperation,
                    "canalId": vm.itemSearch.canalId,
                    "typeService": vm.typeService,
                    "montantOp": vm.itemSearch.montantOp,
                    "dateOperation": vm.itemSearch.dateOperation,
                    "uoAcquereurId": vm.itemSearch.uoAcquereurId,
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: RequestUtils.OperationAttente,
                entitySearch: RequestUtils.OperationAttente,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
        }
        vm.detailsOperations = function (event, operation) {
            console.log("details des operations");
            $uibModal.open({
                templateUrl: 'app/entities/operation/account-cash/account-cash.html',
                controller: 'AccountCashController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity: function () {
                        return operation;
                    },
                    isViewing: true,
                    typeService: function () {
                        return operation.code;
                    },
                    isMaj: true
                }
            });
        };

    }
})();
