(function() {
    "use strict";

    angular.module("ptiApp").factory('InitCrud', ['TableService','RequestUtils','Notification', function InitCrud(TableService,RequestUtils,Notification) {


        return {
            "init": init,
        };

       
        function init(datas) {
            var vm = datas.vm,
                entityDatas = datas.entityDatas,
                entity = datas.entity,
                addTemplateUrl = datas.addTemplateUrl,
                deleteTemplateUrl = datas.deleteTemplateUrl,
                controller = datas.controller,
                entityParamsItem = angular.copy(datas.entityParamsItem),
                entitySearchParamsItem = angular.copy(datas.entitySearchParamsItem),
                size = datas.size,
                scope = datas.scope;
                vm.items=datas.items;
                vm.itemsBackup=datas.items;
                vm.filtrer=false;
                vm.openCalendar=openCalendar;
                vm.openCalendar1=openCalendar1;
                vm.datePickerOpenStatus = {};
                vm.datePickerOpenStatus.dateOperation = false;
                function openCalendar(date) {
                    vm.datePickerOpenStatus[date] = true;
                }

                function openCalendar1(date){
                    vm.datePickerOpenStatus[date] = true;
                }
        
                //Annuler le filtre
                vm.initFiltre=function(){
                    vm.filtrer=!vm.filtrer;
                    if(!vm.filtrer){
                        if(angular.isDefined(vm.isMasterDetails)&&vm.isMasterDetails){
                            datas.state.go(datas.statePrincipal);
                        }
                        entityParamsItem=angular.copy(datas.entityParamsItem);  
                        initTables(); 
                    }
                    vm.reset();
                }       
                
                
                vm.reset=function(){
                    var i=0;
                    for(i=0;i<vm.items.length;i++){
                        angular.forEach(vm.items[i],function(item,value){
                            console.log("reset");
                            item.model=null;
                        });
                    }
                }

                //Lancer le filtre
                vm.filtre=function(){
                    console.log("lancer le filtre");
                    if(angular.isDefined(vm.isMasterDetails)&&vm.isMasterDetails){
                        datas.state.go(datas.statePrincipal);
                    }
                    var i=0;
                    for(i=0;i<vm.items.length;i++){
                        angular.forEach(vm.items[i],function(item,value){
                                if(item.model!=null){
                                    entityParamsItem[item.name]=item.model;
                                }
                        });
                    }
                    initTables();
                };

                vm.exportAll= function exportAll() {
                    var i=0;
                    for(i=0;i<vm.items.length;i++){
                        angular.forEach(vm.items[i],function(item,value){
                                if(item.model!=null){
                                    entityParamsItem[item.name]=item.model;
                                }
                        });
                    }
                    RequestUtils.ExportCommissionCsv.get(entityParamsItem, function (datas) {
                        Notification.success("Generation effectuée avec succès!Fichier disponible dans votre repertoire utilisateur");
                    });
        
                }




            if(angular.isUndefined(datas.bodyClass)) {
                vm.bodyClass ="col-sm-12";
            }else{
                vm.bodyClass = datas.bodyClass;
            }

            if(angular.isUndefined(datas.hasFilter)){
                vm.hasFilter=false;
            }else{
                vm.hasFilter=datas.hasFilter;
            }

            if (angular.isUndefined(datas.headerClass)) {
                vm.headerClass = "col-sm-10 col-sm-offset-1 mt5";
            } else {
                vm.headerClass = datas.headerClass;
            }
            vm.state = datas.detailsState;
            vm.isMasterDetails = datas.isMasterDetails;

            initTables();
            function initTables(){
                var addItemData = {
                    templateUrl: 'app/entities/' + addTemplateUrl,
                    controller: controller + 'DialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: size,
                    resolve: {
                        entity: function() {
                            return angular.copy(entityDatas);
                        },
                        isViewing: false
                    }
                };
    
                /**
                 *Modification d'un élément
                 */
                var editItemData = {
                    templateUrl: 'app/entities/' + addTemplateUrl,
                    controller: controller + 'DialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: size,
                    resolve: {
                        entity: null,
                        isViewing: false
                    }
                };
    
                /**
                 * Suppresion d'un élément
                 */
                var deleteItemData = {
                    templateUrl: 'app/entities/' + deleteTemplateUrl,
                    controller: controller + 'DeleteController',
                    controllerAs: 'vm',
                    size: 'sm',
                    resolve: {
                        entity: null
                    }
                };
    
                var detailItemData = {
                    templateUrl: 'app/entities/' + addTemplateUrl,
                    controller: controller + 'DialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: size,
                    resolve: {
                        entity: null, //valeur déterminé à l'appel de la boite
                        isViewing: true
                    }
                };
    
                var dialogData = {
                    addData: addItemData,
                    editData: editItemData,
                    deleteData: deleteItemData,
                    detailData: detailItemData
                };
                var entitySearchParams = function() {
                    return entitySearchParamsItem;
                };
    
                var entityParams = function() {
                    return entityParamsItem;
                };
    
                vm.datas = {
                    scope: scope,
                    vm: vm,
                    entity: entity,
                    entitySearch: entity,
                    dialogData: dialogData,
                    entitySearchParams: entitySearchParams,
                    entityParams: entityParams
                };
                console.log('entityParamsItem',entityParamsItem);
                TableService.init(vm.datas);
            }
        }
    }]);
})();
