(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('MotifOperationController', MotifOperationController);

    MotifOperationController.$inject = ['InitCrud','$scope','$state', 'MotifOperation', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function MotifOperationController(InitCrud,$scope,$state, MotifOperation, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.title = "Liste des motifs";
        vm.columns = [{
            name: 'libelle',
            title: 'Libelle'
        },{
            name: 'code',
            title: 'Code'
        },{
            name: 'id',
            title: 'Id'
        }];
        
        vm.actions = {
            view: false,
            edit: true,
            delete: true,
        };

        var datas = {
            vm: vm,
            entityDatas: {
                code: null,
                libelle: null,
                id: null
            },
            entity: MotifOperation,
            addTemplateUrl: "motif-operation/motif-operation-dialog.html",
            deleteTemplateUrl: "motif-operation/motif-operation-delete-dialog.html",
            controller: "MotifOperation",
            entityParamsItem: {},
            entitySearchParamsItem: {},
            size: "md",
            scope: $scope
        };
        InitCrud.init(datas);
    }
})();