(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('RoleValidationController', RoleValidationController);

    RoleValidationController.$inject = ['$state', '$scope','RoleValidation','TableService' ];

    function RoleValidationController($state,$scope ,RoleValidation,TableService) {
        var vm = this;
        vm.init=init;
        init();
        function init() {
            var addItemData ={
                templateUrl: 'app/entities/role-validation/role-validation-dialog.html',
                controller: 'RoleValidationDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return {
                            typeServiceId: null,
                            operateur: null,
                            montant: "",
                            roleName: null,
                            id: null
                        };
                    }
                }
            };

            /**
             *Modification d'un élément
             */
            var editItemData = {
                templateUrl: 'app/entities/role-validation/role-validation-dialog.html',
                controller: 'RoleValidationDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: null
                }
            };

            /**
             * Suppresion d'un élément
             */
            var deleteItemData ={
                templateUrl: 'app/entities/role-validation/role-validation-delete-dialog.html',
                controller: 'RoleValidationDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null
                }
            };

            var detailItemData = {
                templateUrl: 'app/entities/role-validation/role-validation-dialog.html',
                controller: 'RoleValidationDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
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
            var entitySearchParams = function() {
                return {
                };
            };

            var entityParams = function() {
                return {

                };
            };

            var datas = {
                scope: $scope,
                vm: vm,
                entity: RoleValidation,
                entitySearch: RoleValidation,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
        }
    }
})();
