(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('InfoSuppController', InfoSuppController);

    InfoSuppController.$inject = ['InitCrud','$scope','$state','InfoSupp', 'ParseLinks', 'AlertService', 'paginationConstants', 'pagingParams'];

    function InfoSuppController(InitCrud,$scope,$state,InfoSupp, ParseLinks, AlertService, paginationConstants, pagingParams) {
        var vm = this;
        vm.title="Liste des informations supplementaire";

        vm.columns = [{
            name: 'libelle',
            title: 'libelle'
        },{
            name: 'description',
            title: 'description'
        }];

        vm.actions={
            view:false,
            edit:true,
            delete:true,
            add:true
        };

        var datas={
            vm:vm,
            entityDatas:{
                libelle: null,
                description: null,
                id: null
            },
            entity:InfoSupp,
            addTemplateUrl:"info-supp/info-supp-dialog.html",
            deleteTemplateUrl:"info-supp/info-supp-delete.html",
            controller:"InfoSupp",
            entityParamsItem:{},
            entitySearchParamsItem:{},
            size:"md",
            scope:$scope
        };
        InitCrud.init(datas);
    }
})();