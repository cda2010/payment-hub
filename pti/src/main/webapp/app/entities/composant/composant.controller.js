(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ComposantController', ComposantController);

    ComposantController.$inject = ['Composant','TableService','$scope'];

    function ComposantController(Composant,TableService,$scope) {

        var vm = this;

        vm.composants = [];
        Composant.query(function(result) {
            vm.totalItems= result.length + 1;
            vm.searchQuery = null;
        });
        //loadAll();
        init();
       /* function loadAll() {
            Composant.query(function(result) {
                vm.composants = result;
                vm.totalItems= result.length + 1;
                vm.searchQuery = null;
            });
        }*/

        function init() {

            var deleteItemData = {
                templateUrl: 'app/entities/composant/composant-delete-dialog.html',
                controller: 'ComposantDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null
                }
            };
            var addItemData = {
                templateUrl: 'app/entities/composant/composant-dialog.html',
                controller: 'ComposantDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function() {
                        return {
                        };
                    }
                }
            };

            var dialogData = {
                addData: addItemData,
                editData: {},
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
                entity: Composant,
                entitySearch: Composant,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);
        }


    }
})();
