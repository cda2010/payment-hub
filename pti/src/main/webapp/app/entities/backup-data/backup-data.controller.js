(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BackupDataController', BackupDataController);

        BackupDataController.$inject = ['$state','BackupData', 'InitCrud','$scope'];

    function BackupDataController($state,BackupData,InitCrud,$scope) {
        var vm = this;
        vm.title = "Liste des Backups";
        vm.create = "Créer ou Modifier"
        vm.columns = [
            {
                name: 'tableSource',
                title: 'Tables'
            },
            {
            name: 'lastBackup',
            title: 'Dernière mise à jour'
            },  
        ];
        vm.actions = {
            view: true,
            edit: true,
            delete: true
        };
       

        var addItemData = {
            templateUrl: 'app/entities/backup-data/backup-data-dialog.html',
            controller: 'BackupDataDialogController',
            controllerAs: 'vm',
            backdrop: 'static',
            size: 'md',
            resolve: {
                entity: function () {
                    return {
                        tableSource: null,
                        tableDest: null,
                        lastBackup: null,
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
                tableSource: null,
                tableDest: null,
                id: null
            }, 
            entity: BackupData,
            addTemplateUrl: "backup-data/backup-data-dialog.html",
            deleteTemplateUrl: "backup-data/backup-data-delete-dialog.html",
            controller: "BackupData",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();
