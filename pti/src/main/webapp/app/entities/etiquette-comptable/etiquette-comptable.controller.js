(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EtiquetteComptableController', EtiquetteComptableController);

    EtiquetteComptableController.$inject = ['$scope','InitCrud','$state', 'EtiquetteComptable', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function EtiquetteComptableController($scope,InitCrud,$state, EtiquetteComptable, ParseLinks, AlertService, paginationConstants, pagingParams) {

        var vm = this;
        vm.title="Liste des etiquettes comptables";

        vm.columns = [{
            name: 'code',
            title: 'Code'
        }];

        vm.actions={
            view:false,
            edit:true,
            delete:true,
        };

        var datas={
            vm:vm,
            entityDatas:{
                code: null,
                id: null
            },
            entity:EtiquetteComptable,
            addTemplateUrl:"etiquette-comptable/etiquette-comptable-dialog.html",
            deleteTemplateUrl:"etiquette-comptable/etiquette-comptable-delete-dialog.html",
            controller:"EtiquetteComptable",
            entityParamsItem:{},
            entitySearchParamsItem:{},
            size:"md",
            scope:$scope
        };
        InitCrud.init(datas);
    }
})();