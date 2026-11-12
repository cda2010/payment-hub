(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TerminalController', TerminalController);

    TerminalController.$inject = ['Terminal','TableService','$scope','$state', 'Profil', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function TerminalController(Terminal,TableService,$scope,$state, Profil, ParseLinks, AlertService, paginationConstants, pagingParams) {

   var vm = this;

        var addItemData ={
            templateUrl: 'app/entities/terminal/terminal-dialog.html',
            controller: 'TerminalDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: function () {
                    return {
                        nom: null,
                        description: null,
                        id: null
                    };
                }
            }
        };

        /**
         *Modification d'un élément
         */
        var editItemData = {
            templateUrl: 'app/entities/terminal/terminal-dialog.html',
            controller: 'TerminalDialogController',
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
            templateUrl: 'app/entities/terminal/terminal-delete-dialog.html',
            controller: 'TerminalDeleteController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                entity: null
            }
        };

        

        var dialogData = {
            addData: addItemData,
            editData: editItemData,
            deleteData: deleteItemData
           
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
            entity: Terminal,
            entitySearch: Terminal,
            dialogData: dialogData,
            entitySearchParams: entitySearchParams,
            entityParams: entityParams
        };
        TableService.init(datas);
    }
})();
