(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ParamCanalController', ParamCanalController);

        ParamCanalController.$inject = ['$state','TableService', 'ParamCanal','InitCrud','$scope'];

    function ParamCanalController($state,TableService, ParamCanal,InitCrud,$scope) {
        var vm = this;
        vm.title = "Liste des etapes par services";
        vm.create = "Créer ou Modifier"
        vm.columns = [
            {
                name: 'typeCanal',
                title: 'Type Canal'
            },
            {
            name: 'application',
            title: 'Application'
            },  
        ];
        vm.actions = {
            view: true,
            edit: true,
            delete: true
        };
        vm.getRandomColor = function() {
            var badgeClasses = ['primary','secondary','success','danger','warning','info','dark'];
            return badgeClasses[Math.floor(Math.random()*badgeClasses.length)];
        };

        var addItemData = {
            templateUrl: 'app/entities/param-canal/param-canal-dialog.html',
            controller: 'ParamCanalDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: function () {
                    return {
                        typeCanal: null,
                        application: null,
                        id: null
                    };
                }
            }
        };
        /**
         *Modification d'un élément
         */
         var editItemData = {
            templateUrl: 'app/entities/param-canal/param-canal-dialog.html',
            controller: 'ParamCanalDialogController',
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
            templateUrl: 'app/entities/param-canal/param-canal-delete-dialog.html',
            controller: 'ParamCanalDeleteController',
            controllerAs: 'vm',
            size: 'sm',  
            resolve: {
                entity: null
            }
        };
        var dialogData = {
            addData: addItemData,
          //  editData: editItemData,
            deleteData: deleteItemData,
            //detailData: detailItemData
        }; 

        /*var datas = {
            scope: $scope,
            vm: vm,
            entity: EtapeService,
            entitySearch: EtapeService,
            dialogData: dialogData,
            entitySearchParamsItem: {},
            entityParamsItem: {}
        };
        TableService.init(datas);*/

        var datas = {
            vm: vm,
            entityDatas: {
                code: null,
                id: null
            }, 
            entity: ParamCanal,
            addTemplateUrl: "param-canal/param-canal-dialog.html",
            deleteTemplateUrl: "param-canal/param-canal-delete-dialog.html",
            controller: "ParamCanal",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
