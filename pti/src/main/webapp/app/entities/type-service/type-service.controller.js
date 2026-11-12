(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TypeServiceController', TypeServiceController);

    TypeServiceController.$inject = ['$scope', 'TableService', '$state', 'TypeService', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function TypeServiceController($scope, TableService, $state, TypeService, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        init();
        function init() {

        var addItemData = {
            templateUrl: 'app/entities/type-service/type-service-dialog.html',
            controller: 'TypeServiceDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: function () {
                    return {
                        libelle: null,
                        cmpteClientSournce: null,
                        cmpteClientDest: null,
                        operationClient: null,
                        fraisApplicable: null,
                        description: null,
                        id: null,
                        etiquetteCompteCode:null
                    };
                }
            }
        };

        /**
         *Modification d'un élément
         */
        var editItemData = {
            templateUrl: 'app/entities/type-service/type-service-dialog.html',
            controller: 'TypeServiceDialogController',
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
        var deleteItemData = {
            templateUrl: 'app/entities/type-service/type-service-delete-dialog.html',
            controller: 'TypeServiceDeleteController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                entity: null
            }
        };

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
            return {};
        };

        var datas = {
            scope: $scope,
            vm: vm,
            entity: TypeService,
            entitySearch: TypeService,
            dialogData: dialogData,
            entitySearchParams: entitySearchParams,
            entityParams: entityParams
        };
        TableService.init(datas);

        }
    }
})();
