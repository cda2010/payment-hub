(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TauxChangeController', TauxChangeController);

    TauxChangeController.$inject = ['InitCrud', 'TauxChange','$scope'];

    function TauxChangeController(InitCrud, TauxChange,$scope) {

        var vm = this;
        vm.title="Liste des taux de change";
        
        vm.columns = [{
            name: 'dateTaux',
            title: 'Date'
        },{
            name: 'typeChange',
            title: 'Type change'
        },{
            name: 'deviseDepartLibelle',
            title: 'Devise depart'
        },{
            name: 'deviseArriveeLibelle',
            title: 'Devise arrivée'
        },{
            name: 'taux',
            title: 'Taux'
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
                dateTaux: null,
                taux: null,
                typeChange: null,
                id: null
            },
            entity:TauxChange,
            addTemplateUrl:"taux-change/taux-change-dialog.html",
            deleteTemplateUrl:"taux-change/taux-change-delete-dialog.html",
            controller:"TauxChange",
            entityParamsItem:{},
            entitySearchParamsItem:{},
            size:"md",
            scope:$scope
        };
        InitCrud.init(datas);
    }
})();