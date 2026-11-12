(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CanalApplicationController', CanalApplicationController);

        CanalApplicationController.$inject = ['$state','TableService', 'CanalApplication','InitCrud','$scope'];

    function CanalApplicationController($state,TableService, CanalApplication,InitCrud,$scope) {
        var vm = this;
        vm.title = "Liste des etapes par services";
        vm.create = "Créer ou Modifier"
        vm.columns = [
            {
                name: 'typeCanal',
                title: 'Type Canal'
            },
            {
            name: 'libelleCanal',
            title: 'Libelle Canal'
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
            templateUrl: 'app/entities/canal-application/canal-application-dialog.html',
            controller: 'CanalApplicationDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: function () {
                    return {
                        typeCanal: null,
                        libelleCanal: null,
                        id: null
                    };
                }
            }
        };
        var dialogData = {
            addData: addItemData,
           // editData: editItemData,
           //deleteData: deleteItemData,
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
               // code: null,
                id: null
            }, 
            entity: CanalApplication,
            addTemplateUrl: "canal-application/canal-application-dialog.html",
            deleteTemplateUrl: "canal-application/canal-application-delete-dialog.html",
            controller: "CanalApplication",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
