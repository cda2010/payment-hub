(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EtapeServiceController', EtapeServiceController);

        EtapeServiceController.$inject = ['$state','TableService', 'EtapeService','InitCrud','$scope'];

    function EtapeServiceController($state,TableService, EtapeService,InitCrud,$scope) {
        var vm = this;
        vm.title = "Liste des etapes par services";
        vm.create = "Créer ou Modifier"
        vm.columns = [
            {
                name: 'typeService',
                title: 'Service'
            },
            {
            name: 'typeService',
            title: 'Etapes'
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
            templateUrl: 'app/entities/etatpe-service/etape-service-dialog.html',
            controller: 'EtapeServiceDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: function () {
                    return {
                        actionService: null,
                        typeService: null,
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
                code: null,
                id: null
            }, 
            entity: EtapeService,
            addTemplateUrl: "etape-service/etape-service-dialog.html",
            deleteTemplateUrl: "etape-service/etape-service-delete-dialog.html",
            controller: "EtapeService",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
